package ru.ifmo.ctddev.solutions.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

/**
 * Class for generating a new class code that implements the specified interface or inherited from the class
 */
public class Implementor implements JarImpler {

    /**
     * Contains a unique list of strings. The string represents the name of the method and its arguments.
     */
    private List<String> allMethods;

    /**
     * Main method to run from console
     * @param args - args[0] - type token to create implementation for.
     *               args[1] - target <tt>.jar</tt> file.
     *
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java -jar Implementor.jar [token] [path to jar with name jar]");
            return;
        }

        try {
            new Implementor().implementJar(Class.forName(args[0]), Paths.get(args[1]));
        } catch (ClassNotFoundException ex) {
            System.out.println("Class not found exception: " + ex.getMessage());
        } catch (ImplerException ex) {
            System.out.println("Impler exception: " + ex.getMessage());
        }
    }

    /**
     *
     * @param token type token to create implementation for.
     * @param jarFile target <tt>.jar</tt> file.
     * @throws ImplerException
     * If the token can not be generated in the jar-file
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        if (!jarFile.toString().toLowerCase().endsWith(".jar")){
            throw new ImplerException("Incorrect name for jarFile");
        }

        Path root;
        if (jarFile.toString().lastIndexOf(File.separator) != -1) {
            root = Paths.get(jarFile.toString().substring(0, jarFile.toString().lastIndexOf(File.separator)));
        }
        else{
            root = Paths.get("").toAbsolutePath();
        }

        implement(token, root);

        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();

        List<String> args = new ArrayList<>();
        args.add("-cp");
        args.add(System.getProperty("java.class.path"));

        String canonicalName = token.getCanonicalName();
        String packagePath = canonicalName.substring(0, canonicalName.lastIndexOf('.')).replace(".", File.separator);
        args.add(root.toString() + File.separator + packagePath + File.separator + token.getSimpleName() + "Impl.java");

        if (javaCompiler == null) {
            throw new ImplerException("Not found java compiler");
        }

        javaCompiler.run(null, null, null, args.toArray(new String[args.size()]));

        String pathToJar = packagePath + File.separator + token.getSimpleName() + "Impl.class";

        try (JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(jarFile))) {

            jarOutputStream.putNextEntry(new JarEntry(pathToJar.replace(File.separator, "/"))); //
            Files.copy(root.resolve(pathToJar), jarOutputStream);
        } catch (IOException ex) {
            throw new ImplerException(ex.getMessage());
        }


    }

    /**
     * @param token type token to create implementation for.
     * @param root root directory.
     * @throws ImplerException
     * If the token can not be implement
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {

        if (token == void.class || token == Enum.class || token.isEnum() || token.isArray()) {
            throw new ImplerException();
        }

        int modifier = token.getModifiers();
        if (Modifier.isFinal(modifier) || Modifier.isPrivate(modifier)) {
            throw new ImplerException("Class can not to be final or private");
        }

        allMethods = new ArrayList<>();
        String canonicalName = token.getCanonicalName();

        boolean isInterface = token.isInterface();

        String classCode = "package " + canonicalName.substring(0, canonicalName.lastIndexOf('.')) + ";\n";
        classCode += "public class " + token.getSimpleName() + "Impl ";
        classCode += (isInterface ? "implements " : "extends ") + canonicalName;
        classCode += "{\n";

        // Generate Constructors

        String constructors = getOverriddenImplementationConstructors(token);

        if (!isInterface && constructors.length() == 0) {
            throw new ImplerException();
        }

        classCode += constructors;

        classCode += getOverriddenImplementationMethods(token);

        // Generate Methods;

        classCode += "}";

        String pathToFileCode = root.toAbsolutePath() + File.separator + canonicalName.replace(".", File.separator) + "Impl.java";

        saveClassAsFile(pathToFileCode, classCode);
    }

    /**
     * Generate methods description for class
     * @param token type token to generate methods.
     * @return string methods description
     */
    private String getOverriddenImplementationMethods(Class<?> token) {
        StringBuilder methods = new StringBuilder();
        String methodName;
        String methodArgs;
        String methodDeclare;
        Class<?> methodReturnType;
        int iModifiers;
        //List<String> allMethods = new ArrayList<>();

        if (token.isInterface()) {
            for (Method method : token.getMethods()) {
                iModifiers = method.getModifiers();
                methodName = method.getName();
                methodArgs = getParametersDescription(method.getParameters());
                methodDeclare = methodName + " " + methodArgs;
                methodReturnType = method.getReturnType();

                if (!Modifier.isFinal(iModifiers) && !allMethods.contains(methodDeclare)) {
                    allMethods.add(methodDeclare);
                    methods.append(Modifier.toString(iModifiers & ~Modifier.ABSTRACT & ~Modifier.TRANSIENT));
                    methods.append(" ");
                    methods.append(methodReturnType.getCanonicalName());
                    methods.append(" ");
                    methods.append(methodDeclare);
                    methods.append(" ");
                    methods.append(getThrowsExceptions(method.getExceptionTypes()));
                    methods.append(" {");
                    methods.append(getReturnDefaultValue(methodReturnType.getTypeName()));

                    methods.append("}\n");
                }
            }
        } else {
            for (Method method : token.getDeclaredMethods()) {
                iModifiers = method.getModifiers();
                methodName = method.getName();
                methodArgs = getParametersDescription(method.getParameters());
                methodDeclare = methodName + " " + methodArgs;
                methodReturnType = method.getReturnType();

                if (!Modifier.isFinal(iModifiers) && !Modifier.isPrivate(iModifiers) && !allMethods.contains(methodDeclare)) {
                    allMethods.add(methodDeclare);
                    methods.append(Modifier.toString(iModifiers & ~Modifier.ABSTRACT & ~Modifier.TRANSIENT & ~Modifier.NATIVE));
                    methods.append(" ");
                    methods.append(methodReturnType.getCanonicalName());
                    methods.append(" ");
                    methods.append(methodDeclare);
                    methods.append(" {");
                    methods.append(getReturnDefaultValue(methodReturnType.getTypeName()));
                    methods.append("}\n");
                }
            }

            Class<?> superClass = token.getSuperclass();
            if (superClass != null) {
                methods.append(getOverriddenImplementationMethods(superClass));
            }
        }

        return methods.toString();
    }

    /**
     * Generate constructors description for class
     * @param token type token to generate constructors
     * @return string constructors description
     */
    private String getOverriddenImplementationConstructors(Class<?> token) {
        StringBuilder constructorsCode = new StringBuilder();
        String name = token.getSimpleName() + "Impl";
        int iModifiers;
        String constructorArgs;
        String constructorDeclare;
        //List<String> constructors = new ArrayList<>();

        for (Constructor constructor : token.getDeclaredConstructors()) {
            iModifiers = constructor.getModifiers();
            constructorArgs = getParametersDescription(constructor.getParameters());
            constructorDeclare = name + " " + constructorArgs;


            if (!Modifier.isFinal(iModifiers) && !Modifier.isPrivate(iModifiers) && !allMethods.contains(constructorDeclare)) {
                allMethods.add(constructorDeclare);
                constructorsCode.append(Modifier.toString(iModifiers & ~Modifier.ABSTRACT & ~Modifier.TRANSIENT));
                constructorsCode.append(" ");
                constructorsCode.append(constructorDeclare);
                constructorsCode.append(" ");
                constructorsCode.append(getThrowsExceptions(constructor.getExceptionTypes()));
                constructorsCode.append(" { super " + getNameParameters(constructor.getParameters()) + ";}\n");

            }
        }

        return constructorsCode.toString();
    }

    /**
     * Generate parameters description
     * @param parameters parameters array for generation
     * @return string parameters description
     */
    private String getParametersDescription(Parameter[] parameters) {
        ArrayList<String> alParameters = new ArrayList<>();
        for (Parameter parameter : parameters) {
            String type = parameter.getType().getCanonicalName();
            String name = parameter.getName();
            alParameters.add(type + " " + name);
        }

        return "(" + String.join(", ", alParameters) + ")";
    }


    /**
     * Generate parameters description without types
     * @param parameters parameters array for generation
     * @return string parameters description without types
     */
    private String getNameParameters(Parameter[] parameters) {
        ArrayList<String> alParameters = new ArrayList<>();
        for (Parameter parameter : parameters) {
            String name = parameter.getName();
            alParameters.add(name);
        }

        return "(" + String.join(", ", alParameters) + ")";
    }

    /**
     * Saves a string description of the code to a file
     * @param pathToFile path to the file to be saved
     * @param classCode code of the stored class
     * @throws ImplerException
     * If writing to a file can not be performed
     */
    private void saveClassAsFile(String pathToFile, String classCode) throws ImplerException {
        File file = new File(pathToFile);
        file.getParentFile().mkdirs();
        try {
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.append(classCode);
            bufferedWriter.newLine();
            bufferedWriter.close();
        } catch (IOException ex) {
            throw new ImplerException(ex.getMessage());
        }
    }

    /**
     * Calculates the default value for the passed type
     * @param exceptions exceptions for implementations
     * @return string description of throwing exceptions
     */
    private String getThrowsExceptions(Class<?>[] exceptions) {
        ArrayList<String> result = new ArrayList<>();

        for (Class<?> exception : exceptions) {
            result.add(exception.getCanonicalName());
        }

        return result.isEmpty() ? "" : "throws " + String.join(",", result);

    }


    /**
     *
     * @param typeName type name for which you want to return the default value
     * @return default value for type name
     */
    private String getReturnDefaultValue(String typeName) {
        if (typeName.equals("byte"))
            return "return 0;";
        if (typeName.equals("short"))
            return "return 0;";
        if (typeName.equals("int"))
            return "return 0;";
        if (typeName.equals("long"))
            return "return 0L;";
        if (typeName.equals("char"))
            return "return '\u0000';";
        if (typeName.equals("float"))
            return "return 0.0F;";
        if (typeName.equals("double"))
            return "return 0.0;";
        if (typeName.equals("boolean"))
            return "return false;";
        if (typeName.equals("void"))
            return "";

        return "return null;";
    }
}
