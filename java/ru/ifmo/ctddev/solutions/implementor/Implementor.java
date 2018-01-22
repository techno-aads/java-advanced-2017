package ru.ifmo.ctddev.solutions.implementor

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

public class Implementor implements JarImpler, Impler {
    private static final String NEW_LINE = System.lineSeparator();

    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        if (!CheckImpl(token)) throw new ImplerException("This class can not be implemented");
        String fileName = "tmp/";
        fileName += token.getPackage().getName().replaceAll("\\.", "/");
        fileName += (token.getPackage().getName().isEmpty()) ? "" : "/";

        String SourceF = GenerateS(token, fileName);
        CompFile(SourceF);

        try {
            PackToJar(SourceF.replace(".java", ".class"), jarFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {

        if (!CheckImpl(token)) throw new ImplerException("This class can not be implemented");

        String fileName = root.toString() + "/";
        fileName += token.getPackage().getName().replaceAll("\\.", "/");
        fileName += (token.getPackage().getName().isEmpty()) ? "" : "/";
        GenerateS(token, fileName);
    }

    public String GenerateS(Class<?> CL, String root) throws ImplerException {

        new File(root).mkdirs();
        String FName = root.toString() + CL.getSimpleName() + "Impl.java";
        try (
                Writer out = new OutputStreamWriter(new FileOutputStream(FName), StandardCharsets.UTF_8);

        ) {

            final String PackName = CL.getPackage().getName();
            out.append("package ").append(PackName).append(";").append(NEW_LINE);
            out.append(NEW_LINE);

            for (Class ImpCL : FindImp(CL)) {
                if (ImpCL.isArray()) {
                    ImpCL = clearFromArray(ImpCL);
                }
                out.append("import ").append(ImpCL.getCanonicalName()).append(";").append(NEW_LINE);
            }
            out.append(NEW_LINE);

            out.append("public class " + CL.getSimpleName() + "Impl");
            if (CL.isInterface())
                out.append(" implements " + CL.getSimpleName()).append(" {");
            else
                out.append(" extends " + CL.getSimpleName()).append(" {");
            out.append(NEW_LINE);

            for (Method m : GetMeth(CL, false)) {
                int Mod = m.getModifiers();
                if (Modifier.isFinal(Mod) || Modifier.isNative(Mod) || Modifier.isPrivate(Mod) || !Modifier.isAbstract(Mod)) {
                    continue;
                }
                Mod ^= Modifier.ABSTRACT;
                if (Modifier.isTransient(Mod)) {
                    Mod ^= Modifier.TRANSIENT;
                }
                out.append(NEW_LINE);
                if (m.isAnnotationPresent(Override.class)) {
                    out.append("    @Override").append(NEW_LINE);
                }
                out.append("    ");
                out.append(Modifier.toString(Mod));

                out.append(" " + m.getReturnType().getSimpleName() + " ");
                out.append(m.getName() + "(");
                Class[] Par = m.getParameterTypes();
                for (int i = 0; i < Par.length; ++i) {
                    out.append(Par[i].getSimpleName() + " " + "arg" + i);
                    if (i < Par.length - 1)
                        out.append(", ");
                }
                out.append(")");
                Class[] Expt = m.getExceptionTypes();
                if (Expt.length != 0) {
                    out.append(" throws ");
                    for (int i = 0; i < Expt.length; ++i) {
                        out.append(Expt[i].getSimpleName());
                        if (i < Expt.length - 1) {
                            out.append(", ");
                        }
                    }
                }
                out.append("{").append(NEW_LINE).append("        return ");
                out.append(GetDefVal(m.getReturnType())).append(";").append(NEW_LINE);
                out.append("    }").append(NEW_LINE);
            }
            Constructor[] c = CL.getDeclaredConstructors();
            boolean DefConst = false;
            if (c.length == 0)
                DefConst = true;
            for (Constructor ctor : c) {
                if (Modifier.isPrivate(ctor.getModifiers()))
                    continue;
                if (ctor.getParameterTypes().length == 0)
                    DefConst = true;
            }
            if (!DefConst) {
                int k = 0;
                while ((Modifier.isPrivate(c[k].getModifiers())))
                    ++k;
                Class[] Par = c[k].getParameterTypes();
                out.append(NEW_LINE);
                out.append("    public " + CL.getSimpleName() + "Impl" + "()");
                if (c[k].getExceptionTypes().length != 0) {
                    out.append(" throws ");
                    Class[] es = c[k].getExceptionTypes();
                    for (int i = 0; i < es.length; ++i) {
                        out.append(es[i].getSimpleName());
                        if (i < es.length - 1)
                            out.append(", ");
                    }
                }
                out.append("{").append(NEW_LINE);
                out.append("        super(");
                for (int i = 0; i < Par.length; ++i) {
                    out.append("(" + Par[i].getSimpleName() + ")");
                    out.append(GetDefVal(Par[i]));
                    if (i < Par.length - 1)
                        out.append(", ");
                }
                out.append(");").append(NEW_LINE);
                out.append("    }");
                out.append(NEW_LINE);
            }
            out.append("}");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return FName;
    }

    private List<Method> GetMeth(Class<?> CL, boolean InRec) {
        List<Method> M = new ArrayList<>();
        if (CL == null)
            return M;
        M.addAll(GetMeth(CL.getSuperclass(), true));
        for (Class inter : CL.getInterfaces()) {
            M.addAll(GetMeth(inter, true));
        }
        for (Method m : CL.getDeclaredMethods()) {

            if (Modifier.isNative(m.getModifiers()) ||
                    Modifier.isStatic(m.getModifiers()) ||
                    m.isSynthetic() || m.isDefault())
                continue;

            if (Modifier.isPublic(m.getModifiers())
                    || Modifier.isProtected(m.getModifiers())
                    || (!Modifier.isProtected(m.getModifiers())
                    && !Modifier.isPublic(m.getModifiers())
                    && !Modifier.isPrivate(m.getModifiers())
                    && !InRec)) {

                if (!ReplDoublMeth(m, M)) {
                    M.add(m);
                }
            }
        }
        return M;
    }

    private boolean ReplDoublMeth(Method NewMeth, List<Method> Meth) {
        m1:
        for (int i = 0; i < Meth.size(); i++) {

            if (NewMeth.getName().equals(Meth.get(i).getName())) {
                Class[] args1 = NewMeth.getParameterTypes();
                Class[] args2 = Meth.get(i).getParameterTypes();
                if (args1.length == args2.length) {
                    for (int j = 0; j < args1.length; ++j) {
                        if (!args1[j].getCanonicalName().equals(args2[j].getCanonicalName())) {
                            //return false;
                            continue m1;
                        }
                    }
                    Meth.set(i, NewMeth);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean ValidImp(Class<?> ImpCL, Class<?> ClientCL) {

        if (ImpCL.isArray()) {
            ImpCL = clearFromArray(ImpCL);
        }
        if (!ImpCL.isPrimitive()
                && !ImpCL.getPackage().getName().startsWith("java.lang")
                && !ImpCL.getPackage().getName().equals(ClientCL.getPackage().getName())) {

            return true;
        }
        return false;

    }

    private Class clearFromArray(Class CL) {
        if (CL.getComponentType().isArray()) {
            return clearFromArray(CL.getComponentType());
        } else {
            return CL.getComponentType();
        }
    }

    public boolean CheckImpl(Class<?> CL) {

        boolean result = false;

        for (Constructor c : CL.getDeclaredConstructors()) {
            if (!Modifier.isPrivate(c.getModifiers())) {
                result = true;
            }
        }
        if (CL.getDeclaredConstructors().length == 0) {
            result = true;
        }
        if (Modifier.isFinal(CL.getModifiers())) {
            result = false;
        }
        if (CL == Enum.class) {
            result = false;
        }
        return result;
    }

    private static String GetDefVal(Class T) {
        if (T.isPrimitive()) {
            if (Boolean.TYPE.equals(T))
                return "false";
            else if (Void.TYPE.equals(T))
                return "";
            else
                return "0";
        } else
            return "null";
    }

    public void CompFile(String F_Path) {
        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
        String[] args = {F_Path, "-cp", F_Path + File.pathSeparator + System.getProperty("java.class.path")};
        int exitCode = -1;
        try {
            exitCode = javaCompiler.run(null, null, null, args);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        if (exitCode != 0) {
            System.out.println("Compilation error, code: " + exitCode);
        }
    }

    private void PackToJar(String SourceF, Path JAR_P) throws IOException {
        FileOutputStream fos = null;
        JarOutputStream jar_output = null;
        try {
            File f = new File(JAR_P.toUri());
            if (f.getParentFile() != null)
                f.getParentFile().mkdirs();
            if (f != null)
                f.createNewFile();
            else {
                System.out.println("invalid path to jar file to be created");
                System.exit(1);
            }
            fos = new FileOutputStream(JAR_P.toFile());
            String New_CL_Path = SourceF.substring(SourceF.indexOf("tmp/") + 4);
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            jar_output = new JarOutputStream(fos, manifest);
            jar_output.putNextEntry(new ZipEntry(New_CL_Path));
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(SourceF));
            int bytesRead;
            byte[] buffer = new byte[8 * 1024];
            while ((bytesRead = bis.read(buffer)) != -1) {
                jar_output.write(buffer, 0, bytesRead);
            }
            jar_output.closeEntry();
            jar_output.close();
            fos.close();
        } catch (IOException e) {
            jar_output.closeEntry();
            jar_output.close();
            fos.close();
            throw new IOException(e.getMessage());
        }

    }

    private Set<Class> FindImp(Class<?> CL) {
        Set<Class> imports = new HashSet<>();
        for (Method method : GetMeth(CL, false)) {

            for (Class paramType : method.getParameterTypes()) {
                if (ValidImp(paramType, CL)) {
                    imports.add(paramType);
                }
            }
            if (ValidImp(method.getReturnType(), CL)) {
                imports.add(method.getReturnType());
            }
            for (Class exceptionType : Arrays.asList(method.getExceptionTypes())) {
                if (ValidImp(exceptionType, CL)) {
                    imports.add(exceptionType);
                }
            }
        }
        for (Constructor ctr : Arrays.asList(CL.getConstructors())) {
            for (Class paramType : ctr.getParameterTypes()) {
                if (ValidImp(paramType, CL)) {
                    imports.add(paramType);
                }
            }
            for (Class exceptionType : Arrays.asList(ctr.getExceptionTypes())) {
                if (ValidImp(exceptionType, CL)) {
                    imports.add(exceptionType);
                }
            }
        }
        return imports;
    }
}
