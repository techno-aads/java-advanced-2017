package ru.ifmo.ctddev.solutions.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
/**
 * Implementor for classes and interfaces
 */
public class Implementor implements Impler, JarImpler {
    /**
     * @see Impler#implement(Class, Path)
     */
    @Override
    public void implement(Class<?> aClass, Path path) throws ImplerException {
        if (aClass.isPrimitive()) {
            throw new ImplerException("Type is a primitive.");
        }
        if (!aClass.isInterface()) {
            if (Modifier.isFinal(aClass.getModifiers())) {
                throw new ImplerException("Cannot extend final class: " + aClass.toString());
            }
        }
        if (Enum.class.isAssignableFrom(aClass)) {
            throw new ImplerException("Cannot extend enum type: " + aClass.toString());
        }

        try {
            File f = formatFile(path, aClass, "java");
            try (Writer writer = new Writer<>(f, aClass)) {
                writer.write();
                writer.flush();
                writer.close();
            }
        }
        catch (Exception e) {
            throw new ImplerException(e);
        }
    }

    /**
     * @see JarImpler#implement(Class, Path)
     * @param aClass base type
     * @param jarPath output jar file
     * @throws ImplerException error during jar file generation
     */
    @Override
    public void implementJar(Class<?> aClass, Path jarPath) throws ImplerException {
        Path directory = jarPath.getParent();
        try {
            implement(aClass, directory);

            Path javaPath = formatPath(directory, formatFile(directory, aClass, "java"));
            String cp = System.getProperty("java.class.path");

            executeCommand(directory, "javac", "-cp", cp, javaPath.toString());

            Path classPath = formatPath(directory, formatFile(directory, aClass, "class"));
            executeCommand(directory, "jar", "cf",
                    jarPath.getFileName().toString(), classPath.toString());
        }
      //  catch (ImplerException e) { throw e; }
        catch (Exception e) { throw new ImplerException(e); }
    }

    /**
     * Writer for interface implementation
     * @param <T> base interface for implementation
     * @see PrintWriter
     */
    protected class Writer<T> extends PrintWriter {
        /**
         * Class for which code is generated
         */
        Class<T> aClass;

        /**
         * Necessary constructors
         */
        List<Constructor<?>> constructors;

        /**
         * Methods which should be implemented or extended
         */
        Collection<Method> methods;

        /**
         * Class will be imported in generated code
         */
        Set<Class<?>> imports;

        /**
         * Package to which class pertains
         * @see #aClass
         */
        Package aPackage;

        Writer(File output, Class<T> aClass) throws Exception {
            super(output);

            try {
                this.aClass = aClass;
                this.methods = collectNecessaryMethods(aClass);
                this.aPackage = aClass.getPackage();
            }
            catch (NullPointerException e) {e.printStackTrace();}
            this.imports = new TreeSet<>(CLASS_COMPARATOR);
            this.imports.add(aClass);
            for (Method method: this.methods) addImportsFrom(method);

            this.constructors = new ArrayList<>();
            for (Constructor<?> constructor: aClass.getDeclaredConstructors()) {
                if (!Modifier.isPrivate(constructor.getModifiers())) {
                    this.constructors.add(constructor);
                    addImportsFrom(constructor);
                }
            }

            if (!aClass.isInterface() && constructors.size() == 0) {
                throw new ImplerException("No default constructor: " + aClass.toString());
            }
        }


        /**
         * Method which validates and extends a collection of imported classes in generated code
         * @param method method for type extraction
         * @see Method
         */
        void addImportsFrom(Method method) {
            addImports(method.getReturnType());
            addImportsFrom((Executable) method);
        }

        /**
         * Method which validates and extends a collection of imported classes in generated code
         * @param executable Executable for type extraction
         * @see Executable
         */
        void addImportsFrom(Executable executable) {
            addImports(executable.getParameterTypes());
            addImports(executable.getExceptionTypes());
        }

        /**
         * Methods which adds types to an import list
         * @param types array of classes
         */
        void addImports(Class<?>... types) {
            for (Class<?> type: types) {
                Class<?> pureType = pureType(type);
                if (validImport(pureType)) {
                    imports.add(pureType);
                }
            }
        }


        /**
         * Method which uses for a code generation
         * (until it's called, code isn't writing to output)
         */
        void write() {
            header();
            start();
            body();
            end();
        }

        /**
         * Method which writes start of a code block to output
         */
        void start() {
            println(" {");
        }

        /**
         * Method which writes end of a code block to output
         */
        void end() {
            println("}");
        }

        /**
         * Method which writes header of a generated class to output
         * @see #write()
         */
        void header() {
            String packageName = aPackage.getName();

            printf("package %s;\n", packageName);
            for (Class<?> iClass: imports) printf("import %s;\n", iClass.getName());

            printf("public class %s", formatClassName(aClass));
            if (!aClass.isInterface()) {
                printf(" extends %s", aClass.getName());
            }
            else {
                printf(" implements %s", aClass.getSimpleName());
            }
        }

        /**
         * Method which writes body of a class for output
         */
        void body() {
            for (Constructor<?> cClass: constructors) {
                printf("%s(%s)", formatClassName(aClass), formatExecutableParams(cClass));
                Class<?>[] exceptions = cClass.getExceptionTypes();

                for (int i = 0; i < exceptions.length; i++) {
                    if (i == 0) {
                        print(" throws ");
                    }
                    else {
                        print(", ");
                    }
                    print(exceptions[i].getSimpleName());
                }

                start();
                Class<?>[] cParams = cClass.getParameterTypes();
                print("super(");
                for (int i = 0; i < cParams.length; i++) {
                    if (i > 0) {
                        print(", ");
                    }
                    printf("arg%d", i);
                }
                println(");");
                end();
            }

            for (Method m: methods) formatMethod(m);
        }

        /**
         * Method which generates string arguments for executable
         * @param executable target executable
         * @return string which contains arguments declaration
         */
        String formatExecutableParams(Executable executable) {
            Class<?>[] args = executable.getParameterTypes();
            List<String> formattedArgs = new LinkedList<>();

            for (int i = 0; i < args.length; i++) {
                String s;
                if (args[i].isArray()) {
                    s = args[i].getComponentType().getName() + "[]";
                }
                else {
                    s = args[i].getName();
                }
                formattedArgs.add(s + " arg" + i);
            }

            return String.join(",", formattedArgs);
        }


        /**
         * Method which gets parent types
         * @param aClass type for extracting parent types
         * @return set of parent classes and interfaces
         */
        Set<Class<?>> getParentTypes(Class<?> aClass) {
            Set<Class<?>> set = new LinkedHashSet<>();

            set.add(aClass);
            for (Class<?> ii : aClass.getInterfaces()) {
                set.addAll(getParentTypes(ii));
            }

            Class<?> superClass = aClass.getSuperclass();
            if (superClass != null) {
                set.addAll(getParentTypes(superClass));
            }

            return set;
        }

        /**
         * Method which takes clear and non-primitive types from class hierarchy
         * @param aClass type for extracting data
         * @return desired type or null
         */
        Class<?> pureType(Class<?> aClass) {
            if (aClass.isArray()) {
                aClass = aClass.getComponentType();
            }
            return aClass.isPrimitive() ? null : aClass;
        }

        /**
         * Method which writes code for method to output
         * @param method method that will be implemented
         */
        void formatMethod(Method method) {
            printf("public %s %s(%s)",
                    methodReturnType(method.getReturnType()),
                    method.getName(),
                    formatExecutableParams(method)
            );

            start();
            Class<?> r = method.getReturnType();
            if (!r.equals(Void.TYPE)) {
                if (r.isPrimitive()) {
                    if (r.equals(Boolean.TYPE)) {
                        println("return false;");
                    }
                    else if (r.equals(Character.TYPE)) {
                        println("return '\\0';");
                    }
                    else {
                        println("return 0;");
                    }
                }
                else {
                    println("return null;");
                }
            }
            end();
        }

        /**
         * Method which extracts necessary methods from class instance
         * (these methods will be implemented by #formatMethod())
         * @param aClass class from which methods will be extracted, including parents
         * @return a methods collection for implementation
         */
        private Collection<Method> collectNecessaryMethods(Class<?> aClass) {
            Set<Method> methods = new TreeSet<>(METHOD_COMPARATOR);
            List<Method> Vreturn = new LinkedList<>();

            for (Class<?> parent: getParentTypes(aClass)) {
                methods.addAll(Arrays.asList(parent.getDeclaredMethods()));
            }

            for (Method m: methods) {
                int mods = m.getModifiers();

                if (Modifier.isFinal(mods) || Modifier.isStatic(mods) || Modifier.isPrivate(mods)) {
                    continue;
                }

                if (m.getName().equals("clone")) {
                    if (Cloneable.class.isAssignableFrom(this.aClass)) {
                        if (m.getParameterTypes().length == 0) {
                            Class<?> returnType = m.getReturnType();

                            int superTypes = 0;
                            for (Method method: methods) {
                                if (method.getName().equals(m.getName())
                                        && method.getParameterTypes().length == 0
                                        && returnType != method.getReturnType()
                                        && returnType.isAssignableFrom(method.getReturnType()))
                                    superTypes++;
                            }

                            if (superTypes > 0) {
                                continue;
                            }
                        }
                    }
                    else {
                        continue;
                    }
                }

                Vreturn.add(m);
            }

            return Vreturn;
        }

        /**
         * @return literal which describes the same type in Java code
         */
        String methodReturnType(Class<?> aClass) {
            if (aClass.isArray()) {
                return aClass.getComponentType().getName() + "[]";
            }
            return aClass.getName();
        }

        /**
         * This method validates if class needs to be imported
         * @param aClass validated class
         * @return it says if it's necessary or not to import the class
         */
        boolean validImport(Class<?> aClass) {
            if (aClass == null || aClass.isLocalClass() || aClass.isMemberClass()) {
                return false;
            }

            Package p = aClass.getPackage();
            return !p.getName().equals("java.lang") && !p.equals(aPackage);
        }

        /**
         * Class implements comparator which permits to avoid importing classes two times
         * Code which contains classes import with the same name can't be compiled successfully
         */
        private final Comparator<Class<?>> CLASS_COMPARATOR = Comparator.comparing(Class::getSimpleName);

        /**
         * Class which permits to left only topmost methods of hierarchy,
         * avoiding duplication or loosing final methods
         */
        private final Comparator<Method> METHOD_COMPARATOR = Comparator.comparing(Method::getName)
                .thenComparing(t -> t.getReturnType().getName(), String::compareTo)
                .thenComparing(t -> t.getParameterTypes().length, Comparator.comparingInt(l -> l))
                .thenComparing(Method::getParameterTypes, (types1, types2) -> {
                    Comparator<Class<?>> comparator = Comparator.comparing(Class::getName);

                    for (int i = 0; i < types1.length; i++) {
                        int v = comparator.compare(types1[i], types2[i]);
                        if (v != 0) return v;
                    }

                    return 0;
                });
    }

    /**
     * @param aClass original class
     * @return generated class name
     */
    private static String formatClassName(Class<?> aClass) {
        return aClass.getSimpleName() + "Impl";
    }

    /**
     * @param directory where generated code will be
     * @param aClass class from which code is generated
     * @return file object for source code file
     * @throws IOException errors during creation of output file
     */
    private static File formatFile(Path directory, Class<?> aClass, String ext) throws IOException {
        String[] packages = aClass.getPackage().getName().split("\\.");
        Path file = Paths.get(directory.toAbsolutePath().toString(), packages);

        Files.createDirectories(file);
        file = Paths.get(file.toString(), formatClassName(aClass) + "." + ext);
        return file.toFile();
    }

    private static Path formatPath(Path dir, File file) {
        return dir.toAbsolutePath().relativize(Paths.get(file.toString()));
    }


    /**
     * Method which executes command with {@link ProcessBuilder}
     * @param workDir working directory of the process
     * @param args arguments of the process
     * @throws IOException {@link ProcessBuilder#start()} error during input/output
     * @throws InterruptedException {@link Process#waitFor()} error thread interrupted during waiting
     */
    private static void executeCommand(Path workDir, String... args) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
        processBuilder.directory(workDir.toFile());
        Process process = processBuilder.start();
        process.waitFor();

    }

    public static void main(String... args) throws Exception {
        if (args.length < 2 || args.length > 3) {
            throw new Exception("Invalid number of arguments.");
        }

        boolean generateJar = args[0].equals("-jar");
        String className, outputFile;

        if (args.length == 3) {
            if (!generateJar) {
                throw new Exception("-jar argument is missing.");
            }
            className = args[1];
            outputFile = args[2];
        }
        else {
            generateJar = false;
            className = args[0];
            outputFile = args[1];
        }

        Implementor implementor = new Implementor();
        Class<?> aClass = Class.forName(className);
        Path file = Paths.get(outputFile);
        if (generateJar) {
            implementor.implementJar(aClass, file);
        }
        else {
            implementor.implement(aClass, file);
        }
    }
}
