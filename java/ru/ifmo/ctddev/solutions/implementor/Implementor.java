package ru.ifmo.ctddev.solutions.implementor;


        import info.kgeorgiy.java.advanced.implementor.ImplerException;
        import info.kgeorgiy.java.advanced.implementor.JarImpler;

        import javax.tools.JavaCompiler;
        import javax.tools.ToolProvider;
        import java.io.*;
        import java.lang.annotation.Annotation;
        import java.lang.reflect.*;
        import java.nio.charset.StandardCharsets;
        import java.util.Collections;
        import java.util.HashMap;
        import java.util.Map;
        import java.util.jar.Attributes;
        import java.util.jar.JarEntry;
        import java.util.jar.JarOutputStream;
        import java.util.jar.Manifest;

public class Implementator implements JarImpler {

    private static void usage() throws ImplerException {
        throw new ImplerException("Usage: Implementator <full.class.name> or Implementator -jar <full.class.name> <file.jar>");
    }

    public static void main(String[] args) throws ImplerException {
        if (args == null) {
            usage();
        }
        if (args.length == 1) {
            if (args[0] == null) {
                usage();
            } else {
                try {
                    new Implementator().implement(Class.forName(args[0]), new File("."));
                } catch (ClassNotFoundException e) {
                    throw new ImplerException("Couldn't find\\open class\\interface " + args[0]);
                }
            }
        } else if (args.length == 3) {
            if (args[0] == null || args[1] == null || args[2] == null || !args[0].equals("-jar")) {
                usage();
            } else {
                try {
                    new Implementator().implementJar(Class.forName(args[1]), new File(args[2]));
                } catch (ClassNotFoundException e) {
                    throw new ImplerException("Couldn't find\\open class\\interface " + args[0]);
                }
            }
        } else {
            usage();
        }
    }

    private static final Map<Class<?>, String> DEFAULT_VALUES;

    static {
        Map<Class<?>, String> defaults = new HashMap<>();
        defaults.put(boolean.class, "false");
        defaults.put(char.class, "0");
        defaults.put(byte.class, "0");
        defaults.put(short.class, "0");
        defaults.put(int.class, "0");
        defaults.put(long.class, "0L");
        defaults.put(float.class, "0f");
        defaults.put(double.class, "0d");
        DEFAULT_VALUES = Collections.unmodifiableMap(defaults);
    }

    private static String getDefaultValue(Class<?> type) {
        return DEFAULT_VALUES.get(type);
    }

    private Class<?> baseClass;

    private void checkCanBeImplemented(Class<?> clazz) throws ImplerException {
        if (clazz == null) {
            throw new ImplerException("Given token is null");
        }
        if (clazz.isPrimitive()) {
            throw new ImplerException("Given token refers to primitive type");
        }
        if (Modifier.isFinal(clazz.getModifiers())) {
            throw new ImplerException("Given token cannot be overridden because it's final");
        }
    }

    private String createRelativePath(Class<?> token) {
        String packageName = token.getPackage().getName();
        return packageName.replaceAll("\\.", File.separator) + File.separator + createImplName(token) + ".java";
    }


    private static final String SUFFIX = "Impl";


    private String createImplName(Class<?> token) {
        return token.getSimpleName() + SUFFIX;
    }

    @Override
    public void implement(Class<?> clazz, File root) throws ImplerException {
        implementToFile(clazz, root);
    }

    private File implementToFile(Class<?> token, File root) throws ImplerException {
        checkCanBeImplemented(token);
        baseClass = token;

        File outputFile = new File(root.getAbsolutePath() + File.separator + createRelativePath(token));
        if (!outputFile.exists()) {
            if (outputFile.getParentFile() != null) {
                if (!outputFile.getParentFile().mkdirs()) {
                }
            }
        }

        try (BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {
            addPackage(output);
            addHeader(output);
            addConstructors(output);
            addMethods(output);
            addClosing(output);
        } catch (FileNotFoundException e) {
            throw new ImplerException("Couldn't create\\open file " + outputFile.getAbsolutePath() + " cause: " + e.getMessage());
        } catch (IOException e) {
            throw new ImplerException("Error during writing class's code");
        }
        return outputFile;
    }


    @Override
    public void implementJar(Class<?> clazz, File jarFile) throws ImplerException {
        if (!jarFile.exists()) {
            if (!jarFile.mkdirs()) {
                throw new ImplerException("Couldn't create specified directories: " + jarFile.getAbsolutePath());
            }
        }

        File implementedFile = implementToFile(clazz, jarFile);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if ((compiler.run(null, null, null, implementedFile.getAbsolutePath())) != 0) {
            throw new ImplerException("Error compiling generated class");
        }

        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        File classFile = new File(replaceLast(implementedFile.getAbsolutePath(), ".java", ".class"));
        try (JarOutputStream target = new JarOutputStream(
                new FileOutputStream(jarFile.getAbsolutePath() + File.separator + createImplName(clazz) + ".jar"), manifest);
             InputStream input = new BufferedInputStream(new FileInputStream(classFile))
        ) {
            String name = replaceLast(createRelativePath(clazz), ".java", ".class");
            JarEntry entry = new JarEntry(name);
            entry.setTime(System.currentTimeMillis());
            target.putNextEntry(entry);
            int count;
            byte[] buffer = new byte[1024];
            while ((count = input.read(buffer)) >= 0) {
                target.write(buffer, 0, count);
            }
            target.closeEntry();
        } catch (IOException e) {
            throw new ImplerException("Error during writing to jar file " + e.getMessage());
        }
    }

    private String replaceLast(String text, String from, String to) {
        int lastIndex = text.lastIndexOf(from);
        if (lastIndex < 0) {
            return text;
        }
        return text.substring(0, lastIndex) + to;
    }


    private void addPackage(BufferedWriter output) throws IOException {
        if (baseClass.getPackage() != null) {
            String packageName = baseClass.getPackage().getName();
            output.write("package " + packageName + ";");
            output.newLine();
        }
    }

    private void addHeader(BufferedWriter output) throws IOException {
        output.write("public class " + baseClass.getSimpleName() + "Impl" +
                (baseClass.isInterface() ? " implements " : " extends ") +
                baseClass.getTypeName() + " {");
        output.newLine();
    }

    private void addConstructors(BufferedWriter output) throws ImplerException, IOException {
        if (baseClass.isInterface()) {
            return;
        }
        Constructor<?> constructors[] = baseClass.getDeclaredConstructors();

        for (Constructor<?> constructor : constructors) {
            if (Modifier.isPublic(constructor.getModifiers()) || Modifier.isProtected(constructor.getModifiers())) {
                if (constructor.getParameters().length == 0) {
                    return;
                }
            }
        }

        for (Constructor<?> constructor : constructors) {
            if (Modifier.isPublic(constructor.getModifiers()) || Modifier.isProtected(constructor.getModifiers())) {
                Parameter parameters[] = constructor.getParameters();
                Type exceptionTypes[] = constructor.getGenericExceptionTypes();

                if (Modifier.isPublic(constructor.getModifiers())) {
                    output.write("\tpublic ");
                } else if (Modifier.isProtected(constructor.getModifiers())) {
                    output.write("\tprotected ");
                }
                output.write(baseClass.getSimpleName() + "Impl" + "(");

                if (parameters.length > 0) {
                    for (int i = 0; i < parameters.length - 1; i++) {
                        output.write(parameters[i].toString() + ", ");
                    }
                    output.write(parameters[parameters.length - 1].toString());
                }
                output.write(")");
                if (exceptionTypes.length > 0) {
                    output.write(" throws " + exceptionTypes[0].getTypeName());
                    for (int i = 1; i < exceptionTypes.length; i++) {
                        output.write(", " + exceptionTypes[i].getTypeName());
                    }
                }
                output.write(" {");
                output.newLine();
                output.write("\t\tsuper(");
                if (parameters.length > 0) {
                    output.write(parameters[0].getName());
                    for (int i = 1; i < parameters.length; i++) {
                        output.write(", " + parameters[i].getName());
                    }
                }
                output.write(");");
                output.newLine();
                output.write("\t}\n");
                output.newLine();
                return;
            }
        }

        //Couldn't add any constructor
        throw new ImplerException();
    }

    private static interface TreeWalkVisitor {
        void onVisitMethod(Method m);
    }

    private void recursiveTreeWalk(Class<?> currentClass, TreeWalkVisitor treeWalkVisitor) {
        if (currentClass == null) {
            return;
        }
        for (Method m : currentClass.getDeclaredMethods()) {
            treeWalkVisitor.onVisitMethod(m);
        }
        for (Class<?> anInterface : currentClass.getInterfaces()) {
            recursiveTreeWalk(anInterface, treeWalkVisitor);
        }
        if (currentClass.getSuperclass() != null) {
            recursiveTreeWalk(currentClass.getSuperclass(), treeWalkVisitor);
        }
    }

    private static String getMethodSignature(Method m) {
        StringBuilder result = new StringBuilder();
        result.append(m.getReturnType().getCanonicalName());
        result.append(m.getName());
        for (Class<?> parameter : m.getParameterTypes()) {
            result.append(parameter.getCanonicalName());
        }
        return result.toString();
    }

    private void addMethods(BufferedWriter output) throws IOException {
        Map<String, Method> necessaryMethods = new HashMap<>();
        recursiveTreeWalk(baseClass, m -> {
            if (Modifier.isAbstract(m.getModifiers())) {
                String signature = getMethodSignature(m);
                if (!necessaryMethods.containsKey(signature)) {
                    necessaryMethods.put(signature, m);
                } else {
                    Class<?> currentClass = m.getDeclaringClass();
                    Class<?> alreadyClass = necessaryMethods.get(signature).getDeclaringClass();
                    if (alreadyClass.isAssignableFrom(currentClass)) {
                        necessaryMethods.put(signature, m);
                    }
                }
            }
        });

        recursiveTreeWalk(baseClass, m -> {
            if (!Modifier.isAbstract(m.getModifiers())) {
                String signature = getMethodSignature(m);
                if (necessaryMethods.containsKey(signature)) {
                    Class<?> currentClass = m.getDeclaringClass();
                    Class<?> alreadyClass = necessaryMethods.get(signature).getDeclaringClass();
                    if (alreadyClass.isAssignableFrom(currentClass)) {
                        necessaryMethods.remove(signature);
                    }
                }
            }
        });

        for (Method method : necessaryMethods.values()) {
            if (Modifier.isAbstract(method.getModifiers())) {
                Parameter parameters[] = method.getParameters();
                Type exceptionTypes[] = method.getGenericExceptionTypes();
                Class<?> returnType = method.getReturnType();

                output.write("\t@Override");
                output.newLine();

                for (Annotation annotation : method.getAnnotations()) {
                    output.write("\t" + annotation.toString());
                    output.newLine();
                }

                if (Modifier.isPublic(method.getModifiers())) {
                    output.write("\tpublic ");
                } else if (Modifier.isProtected(method.getModifiers())) {
                    output.write("\tprotected ");
                } else {
                    output.write("\t");
                }

                Type type = method.getGenericReturnType();
                if (type instanceof TypeVariable) {
                    output.write("<" + ((TypeVariable) type).getName() + "> ");
                }

                output.write(method.getGenericReturnType().getTypeName() +  " " + method.getName() + "(");
                if (parameters.length > 0) {
                    output.write(parameters[0].toString());
                    for (int i = 1; i < parameters.length; i++) {
                        output.write(", " + parameters[i].toString());
                    }
                }
                output.write(")");
                if (exceptionTypes.length > 0) {
                    output.write(" throws " + exceptionTypes[0].getTypeName());
                    for (int i = 1; i < exceptionTypes.length; i++) {
                        output.write(", " + exceptionTypes[i].getTypeName());
                    }
                }

                output.write(" {");
                output.newLine();
                if (returnType != Void.TYPE) {
                    if (returnType.isArray()) {
                        int dimension = 1;
                        Class<?> arrayClass = returnType;
                        while (arrayClass.getComponentType() != null) {
                            dimension++;
                            arrayClass = arrayClass.getComponentType();
                        }
                        output.write("\t\treturn new " + arrayClass.getCanonicalName());
                        output.write("[0]");
                        for (int i = 1; i < dimension - 1; i++) {
                            output.write("[]");
                        }
                        output.write(";");
                        output.newLine();
                    } else {
                        output.write("\t\treturn " + getDefaultValue(returnType) + ";");
                        output.newLine();
                    }
                }
                output.write("\t}\n");
                output.newLine();
            }
        }
    }

    private void addClosing(BufferedWriter output) throws IOException {
        output.write("}");
        output.newLine();
    }
}