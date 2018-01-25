package ru.ifmo.ctddev.solutions.implementor;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.*;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

/**
 * Amazing class which produces meaningless, but compileable code.
 * Does both classes and JARs.
 *
 */
public class Implementor implements  JarImpler {
    /**
     * flag, true when JAR generation was requested
     */
    private boolean generateJar;

    /**
     * Constructs Implementor, setting {@link #generateJar} flag to {@code false}.
     */
    public Implementor() {
        generateJar = false;
    }

    /**
     * Returns string of access modifier of by {@link java.lang.Class class}, {@link java.lang.reflect.Constructor constructor} or {@link java.lang.reflect.Method method}.
     *
     * @param modifiers {@code modifiers} flag provided by {@code getModifiers()} of either {@link java.lang.Class class}, {@link java.lang.reflect.Constructor constructor} or {@link java.lang.reflect.Method method}
     * @return string representation of access modifier.
     */
    private static String printModifiers(int modifiers) {
        List<String> modifiersStr = new LinkedList<>();

        if (Modifier.isPublic(modifiers)) {
            modifiersStr.add("public");
        } else if (Modifier.isPrivate(modifiers)) {
            modifiersStr.add("private");
        } else if (Modifier.isProtected(modifiers)) {
            modifiersStr.add("protected");
        }

        return String.join(" ", modifiersStr);
    }

    /**
     * Returns comma-and-space separated string representation of type parameters.
     *
     * @param typeParams array of parameters to be stringified
     * @return comma-and-space separated string representation of {@code typeParams}
     */
    private String printTypeParams(Class[] typeParams) {
        return String.join(", ", Arrays.asList(typeParams).stream().map(Class::getCanonicalName).collect(Collectors.toList()));
    }

    /**
     * Returns all methods of class and all its superclasses and interfaces.
     *
     * @param clazz class which methods we are interested in.
     * @return list of all methods
     */
    private List<Method> getAllMethods(Class<?> clazz) {
        List<Method> methods = new ArrayList<>();
        Class<?> it = clazz;

        while (it != null && it != Object.class) {
            Collections.addAll(methods, it.getDeclaredMethods());
            Collections.addAll(methods, it.getMethods());
            it = it.getSuperclass();
        }

        return methods;
    }

    /**
     * Changes the value of {@code generateJar}.
     *
     * @param genJar new value of flag
     */
    public void setGenerateJar(boolean genJar) {
        generateJar = genJar;
    }

    /**
     * Produces code implementing class or interface specified by provided <tt>token</tt>.
     * <p>
     * Generated class full name should be same as full name of the type token with <tt>Impl</tt> suffix
     * added. Generated source code should be placed in the correct subdirectory of the specified
     * <tt>root</tt> directory and have correct file name. For example, the implementation of the
     * interface {@link java.util.List} should go to <tt>$root/java/util/ListImpl.java</tt>
     *
     * @param clazz type token to create implementation for.
     * @param root root directory.
     * @throws ImplerException in the following cases:<ol>
     *      <li>if {@code clazz} is a primitive class</li>
     *      <li>if {@code clazz} is final and, therefore, cannot be extended</li>
     *      <li>if {@code clazz} does not contain a public or protected constructor</li>
     *      </ol>
     */
    @Override
    public void implement(Class<?> clazz, Path root) throws ImplerException {
        if (clazz.isPrimitive()) {
            throw new ImplerException("primitive type");
        }
        if (Modifier.isFinal(clazz.getModifiers())) {
            throw new ImplerException("final class");
        }

        StringBuilder implStr = new StringBuilder();
        String className = clazz.getSimpleName() + "Impl";
        String classCanonName = clazz.getCanonicalName();

        implStr.append("package " + clazz.getPackage().getName() + ";\n\n");

        implStr.append("public class " + className + " ");
        if (clazz.isInterface()) {
            implStr.append("implements");
        } else {
            implStr.append("extends");
        }
        implStr.append(" " + classCanonName + " {\n");

        Constructor[] constructors = clazz.getDeclaredConstructors();
        boolean isConstructible = constructors.length == 0;
        for (Constructor c : constructors) {
            if (!Modifier.isPrivate(c.getModifiers())) {
                isConstructible = true;
            }
            implStr.append("\n\tpublic " + className + "(");
            int varCount = 0;
            boolean firstParam = true;
            for (Class cc : c.getParameterTypes()) {
                if (!firstParam) {
                    implStr.append(", ");
                }
                implStr.append(cc.getTypeName() + " v" + (++varCount));
                firstParam = false;
            }

            implStr.append(")");

            String cExceptionTypes = printTypeParams(c.getExceptionTypes());
            if (!cExceptionTypes.isEmpty()) {
                implStr.append(" throws " + cExceptionTypes);
            }

            implStr.append(" {\n\t\tsuper(");
            for (int i = 1; i <= varCount; i++) {
                if (i != 1) {
                    implStr.append(", ");
                }
                implStr.append("v" + i);
            }
            implStr.append(");\n\t}\n");
        }

        if (!isConstructible) {
            throw new ImplerException("no default constructor");
        }

        Set<UniqueMethod> methods = new HashSet<>();
        for (Method m : getAllMethods(clazz)) {
            int modifiers = m.getModifiers();
            if (Modifier.isAbstract(modifiers)) {
                methods.add(new UniqueMethod(m));
            }
        }

        for (UniqueMethod um : methods) {
            Method m = um.getMethod();
            int modifiers = m.getModifiers();
            Class retType = m.getReturnType();
            String typeName = m.getReturnType().getCanonicalName();
            implStr.append("\n\t" + printModifiers(modifiers) + " ");
            implStr.append(typeName + " " + m.getName() + "(");

            int varCount = 0;
            boolean firstParam = true;
            for (Class c : m.getParameterTypes()) {
                if (!firstParam) {
                    implStr.append(", ");
                }
                implStr.append(c.getCanonicalName() + " v" + (++varCount));
                firstParam = false;
            }

            implStr.append(")");

            String cExceptionTypes = printTypeParams(m.getExceptionTypes());
            if (!cExceptionTypes.isEmpty()) {
                implStr.append(" throws " + cExceptionTypes);
            }

            implStr.append(" {\n");
            if (!retType.equals(void.class)) {
                implStr.append("\t\treturn ");
                if (retType.isPrimitive()) {
                    if (retType.equals(boolean.class)) {
                        implStr.append("false");
                    } else {
                        implStr.append("0");
                    }
                } else {
                    implStr.append("null");
                }
                implStr.append(";\n");
            }

            implStr.append("\t}\n");
        }

        implStr.append("}\n");

        File outputFile = new File(root.toFile(), clazz.getCanonicalName().replace(".", File.separator) + "Impl.java");
        outputFile.getParentFile().mkdirs();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "utf8"))) {
            writer.write(implStr.toString());
        } catch (IOException e) {
            throw new ImplerException(e);
        }

        if (generateJar) {
            compileFile(root.toFile(), outputFile);
            createJar(root.toFile(), clazz);
        }
    }

    /**
     * Produces <tt>.jar</tt> file implementing class or interface specified by provided <tt>token</tt>.
     * <p>
     * Generated class full name should be same as full name of the type token with <tt>Impl</tt> suffix
     * added.
     *
     * @param token type token to create implementation for.
     * @param jarFile target <tt>.jar</tt> file.
     * @throws ImplerException when implementation cannot be generated (see {@link #implement(Class, Path)}).
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        boolean prevGenerateJar = generateJar;
        setGenerateJar(true);
        implement(token, jarFile);
        setGenerateJar(prevGenerateJar);
    }


    /**
     * Compiles <tt>source</tt> file in <tt>root</tt> directory.
     * <p>
     * Generated class full name should be same as full name of the type token with <tt>Impl</tt> suffix
     * added.
     *
     * @param root root directory.
     * @param source source <tt>.java</tt> file.
     * @throws ImplerException when compilation error happens.
     */
    private void compileFile(File root, File source) throws ImplerException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        List<String> args = new ArrayList<>();
        args.add(source.getPath());
        args.add("-cp");
        args.add(root.getPath() + File.pathSeparator + System.getProperty("java.class.path"));

        int exitCode = compiler.run(null, null, null, args.toArray(new String[args.size()]));
        if (exitCode != 0) {
            throw new ImplerException("error during compile");
        }
    }

    /**
     * Creates jar in <tt>classFile</tt> for class <tt>clazz</tt> in <tt>root</tt> directory.
     *
     * @param root root directory.
     * @param clazz type token to create jar for.
     * @throws ImplerException when {@link java.io.IOException} it occurs in {@link #addToJar(File, JarOutputStream)}.
     */
    private void createJar(File root, Class<?> clazz) throws ImplerException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        File jarFile = new File(root, clazz.getSimpleName() + "Impl.jar");
        File pathToClasses = new File(clazz.getPackage().getName().split("\\.")[0]);
        try (JarOutputStream stream = new JarOutputStream(new FileOutputStream(jarFile), manifest)) {
            addToJar(pathToClasses, stream);
        } catch (IOException e) {
            throw new ImplerException(e);
        }
    }

    /**
     * Adds <tt>file</tt> in <tt>stream</tt>.
     *
     * @param file file to be added.
     * @param stream jar stream to add file in.
     * @throws IOException if it is thrown while reading <tt>file</tt>.
     */
    private void addToJar(File file, JarOutputStream stream) throws IOException {
        if (file.isDirectory()) {
            JarEntry entry = new JarEntry(file.getPath());
            stream.putNextEntry(entry);
            stream.closeEntry();

            for (File nestedFile : file.listFiles()) {
                addToJar(nestedFile, stream);
            }
        } else if (file.getName().endsWith(".class")) {
            JarEntry entry = new JarEntry(file.getPath());
            stream.putNextEntry(entry);

            BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
            byte[] buffer = new byte[4096];
            int count;

            while ((count = in.read(buffer)) != -1) {
                stream.write(buffer, 0, count);
            }

            stream.closeEntry();
        }
    }

    /**
     * Wrapper for {@link java.lang.reflect.Method Method} class, tells method with different signatures apart.
     *
     * @see java.lang.reflect.Method
     */
    private static class UniqueMethod {
        /**
         * Wrapped method.
         */
        private final Method m;

        /**
         * Constructs a new <tt>UniqueMethod</tt>.
         *
         * @param m method to be wrapped.
         */
        public UniqueMethod(Method m) {
            this.m = m;
        }

        /**
         * Returns wrapped <tt>method</tt>.
         *
         * @return method that was wrapped.
         */
        public Method getMethod() {
            return m;
        }

        /**
         * Calculates hash code of a <tt>method</tt>.
         *
         * @return hash code of a wrapped method.
         */
        public int hashCode() {
            int hash = m.getName().hashCode();
            for (Class c : m.getParameterTypes()) {
                hash ^= c.hashCode();
            }
            return hash;
        }

        /**
         * Checks if type signatures of two <tt>method</tt>s equal.
         *
         * @param obj another object.
         * @return <tt>true</tt>, if two methods have equal signature. <tt>false</tt> otherwise.
         */
        public boolean equals(Object obj) {
            if (obj instanceof UniqueMethod) {
                Method other = ((UniqueMethod) obj).getMethod();
                if (!m.getName().equals(other.getName())) {
                    return false;
                }

                if (!Arrays.equals(m.getParameterTypes(), other.getParameterTypes())) {
                    return false;
                }
                return true;
            } else {
                return false;
            }
        }
    }
}