package ru.ifmo.ctddev.ovsyannikov.implementor;

import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import java.util.jar.*;
import java.util.zip.ZipEntry;
import javax.tools.*;


import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;



/**
 * Class for implementing interfaces  {@link Impler} and {@link JarImpler}
 * @see info.kgeorgiy.java.advanced.implementor.JarImpler
 * @author Tim Ovsyannikov
 */
public class Implementor implements JarImpler
{   /**
    * LINE SEPARATOR
    */
    final private String LS = System.lineSeparator();

    /**
    * FILE SEPARATOR
    */
    final private String FS = getFS();
    /**
     *  Class example which implementation is being created
     */
    private Class<?> classExample;
    /**
     *  Full name of the interface
     */
    private String className;
    /**
     * Methods array of implemented interface
     */
    private Method[] methods;
    /**
     * Package name of interface
     */
    private String packageName;
    /**
     * Class comparator
     */
    private Comparator<Class<?>> CLASS_COMPARATOR = Comparator.comparing(Class::getSimpleName);
    /**
     *  Imports required for implemented interface
     */
    private Set<Class<?>> imports;
    /**
     *  Output file
     */
    private File out;

    /**
     * Getting file separator
     * @return string which contains current file separator
     */
    private static String getFS()
    {
        return File.separator.equals("\\") ? "\\\\" : File.separator;
    }
    /**
     * Method which generates the interface code and writes it into the file, which is placed in directory stated in
     * path variable.
     * @param token class example which is implemented
     * @param path path to directory or .java file
     * @throws ImplerException throws if can't implement interface
     */
    @Override
    public void implement(Class<?> token, Path path) throws ImplerException
    {
        validator(token);
        out = path.toString().contains("Impl.java")
            ? path.toFile()
            : getOutputFile(token, path);

        try ( Writer writer = new OutputStreamWriter( new FileOutputStream(out.getPath()), StandardCharsets.UTF_8 ) )
        {
            String implementation = getImplementedClass(token);

            writer.write(implementation);
            writer.close();
        }
        catch (Exception e)
        {
            throw new ImplerException(e);
        }
    }

    /**
     * Method which generates code and returns it.
     * @param currentInterface class example currently being implemented
     * @return String with generated implementation
     */
    private String getImplementedClass(Class<?> currentInterface)
    {
        extractClassParams(currentInterface);
        return generateHeader() + generateBody() + "}";
    }


    /**
     * Method which reads the data required to implement requested class code.
     * @param currentInterface class example currently being implemented
     */
    private void extractClassParams(Class<?> currentInterface)
    {
        this.classExample = currentInterface;
        this.className = currentInterface.getSimpleName();
        this.packageName = currentInterface.getPackage().getName();
        this.methods = currentInterface.getMethods();
        this.imports = new TreeSet<>(CLASS_COMPARATOR);

        this.imports.add(currentInterface);

        for (Method m : methods)
        {
            addImportsFrom(m);
        }
    }

    /**
     *  Method for getting the required package imports
     * @param method method for which package is imported
     */
    void addImportsFrom(Method method)
    {
        addImports(method.getReturnType());
        addImportsFrom((Executable) method);
    }

    /**
     * Extract additional package imports
     * @param executable Executable object for extraction
     */
    void addImportsFrom(Executable executable)
    {
        addImports(executable.getParameterTypes());
        addImports(executable.getExceptionTypes());
    }

    /**
     * Getting list of required import packages
     * @param types class types
     */
    private void addImports(Class<?>... types)
    {
        this.imports.addAll(Arrays.stream(types)
                .map(this::getType)
                .filter(this::isValid)
                .collect(Collectors.toList())
        );
    }

    /**
     * Method that returns the type of class
     * @param aClass class example currently being implemented
     * @return class example type if it's not a primitive type
     */
    private Class<?> getType(Class<?> aClass)
    {
        if (aClass.isArray())
        {
            aClass = aClass.getComponentType();
        }
        return aClass.isPrimitive() ? null : aClass;
    }

    /**
     * Validates if class need to be imported
     * @param aClass class example currently being implemented
     * @return true if class need to import
     */
    private boolean isValid(Class<?> aClass)
    {

        if (aClass == null)
        {
            return false;
        }

        if (aClass.isLocalClass() || aClass.isMemberClass())
        {
            return false;
        }

        Package p = aClass.getPackage();

        return !p.getName().equals("java.lang") && !p.equals(classExample.getPackage());
    }


    /**
     * Reading the data required to implement requested class code.
     * @param classExample class example currently being implemented
     * @param path path where generated file need save
     * @return file for writer
     * @throws ImplerException throws if error occurs during path determination
     */
    private File getOutputFile(Class<?> classExample, Path path) throws ImplerException
    {
        try {
            String classFileName = classExample.getSimpleName() + "Impl.java";
            String[] packages = classExample.getPackage().getName().split("\\.");
            Path outputPath = Paths.get(path.toAbsolutePath().toString(), packages);


            Files.createDirectories(outputPath);
            outputPath = Paths.get(outputPath.toString(), classFileName);

            System.out.println("Filename =" + outputPath);
            return outputPath.toFile();
        }
        catch (IOException e) { throw new ImplerException(e); }
    }

    /**
     * Generating the code for the header of the class declaration
     * @return String with implemented header
     */
    private String generateHeader()
    {
        StringBuilder headerStruct = new StringBuilder();

        headerStruct.append("package ").append(packageName).append(";").append(LS);

        for (Class<?> s : imports)
        {
            headerStruct.append("import ").append(s.getName()).append(";").append(LS);
        }

        headerStruct.append(LS);
        headerStruct.append("public class ").append(className).append("Impl implements ").append(className).append(" {").append(LS);

        return headerStruct.toString();
    }

    /**
     * Generating the code for the body of the class declaration
     * @return String with body of implemented class
     */
    private String generateBody()
    {
        StringBuilder body = new StringBuilder();
        for (Method m : methods)
        {
            body.append(implementMethod(m));
        }
        return body.toString();

    }

    /**
     * Generating the code for the method of interface
     * @param method method of the interface
     * @return string that contains code for implemented method
     */
    private String implementMethod(Method method)
    {
        StringBuilder methodStruct = new StringBuilder();
        methodStruct.append("    public ").append(method.getReturnType().getSimpleName())
                .append(" ").append(method.getName()).append("(" + methodParams(method) + ") {").append(LS);

        Class<?> returnType = method.getReturnType();
        if (!returnType.equals(Void.TYPE)) {
            methodStruct.append("        ");
            if (returnType.isPrimitive()) {
                if (returnType.equals(Boolean.TYPE)) {
                    methodStruct.append("return false;").append(LS);
                } else if (returnType.equals(Character.TYPE)) {
                    methodStruct.append("return '\\0';").append(LS);
                } else {
                    methodStruct.append("return 0;").append(LS);
                }
            } else {
                methodStruct.append("return null;").append(LS);
            }
        }

        methodStruct.append("    }").append(LS).append(LS);

        return methodStruct.toString();
    }


    /**
     * Method that returns the string of parameters for method
     * @param m method in which parameters are extracted
     * @return String with extracted method parameters
     */
    String methodParams(Method m)
    {
        Class<?>[] params = m.getParameterTypes();

        return IntStream.range(0, params.length).mapToObj(i ->
        {
            String s;
            if (params[i].isArray())
            {
                s = params[i].getComponentType().getName() + "[]";
            } else {
                s = params[i].getName();
            }

            return s + " arg" + i;

        }).collect(Collectors.joining(", "));
    }

/**
*    Main method. Depending on command line arguments calls either implement() method or implementJar() method.
 *    If the argument contains "-jar" key, the method will implement an interface and create .jar file.
 *    Otherwise only the interface will be implemented.
 *   @param args Arguments array of the command line for the method.
*/

    public static final void main(String[] args)
    {
        if (args.length < 2)
        {
            System.out.println("Not enough arguments");
            System.exit(1);
        }

        if (args.length > 3)
        {
            System.out.println("Too much arguments");
            System.exit(1);
        }

        if ((args.length == 3) && (!args[0].equals("-jar")))
        {
            System.out.println("Not Correct file extension");
            System.exit(1);
        }

        try
        {
            if (args[0].equals("-jar"))
            {
                new Implementor().implementJar(Class.forName(args[1]), Paths.get(args[2]));
            }
            else
            {
                new Implementor().implement(Class.forName(args[0]), Paths.get(args[1]));
            }
        }
        catch (ImplerException e)
        {
            System.out.println("ImplerException: " + e.getMessage());
        }
        catch (InvalidPathException e)
        {
            System.out.println("InvalidPathException: " + e.getMessage());
        }
        catch (ClassNotFoundException e)
        {
            System.out.println("No such class: " + e.getMessage());
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            System.out.println("Not enough arguments were inputted in main() method");
        }
    }

    /**
     * Implementation of interface code and creating .jar file.
     * @param token class example currently being implemented
     * @param jarFile path where generated .jar file will be saved
     * @throws ImplerException throws if can't implement interface
     */
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException
    {
        try
        {
        validator(token);

        classExample = token;

        Package pack = token.getPackage();
        String sPack = pack == null ? "" : pack.getName();

        className = "tmp" + FS + sPack.replaceAll("\\.", FS);
        className += (sPack.isEmpty()) ? "" : FS;
        className += token.getSimpleName() + "Impl.java";

        implement(classExample, Paths.get(className));

        compileFile(out.getPath());


            String sourceFile = out.getPath().replaceAll(FS, "/");

            createJarFile(sourceFile.replace(".java", ".class"), jarFile);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Compiling generated java-class file
     * @param filePath path to java-class.
     */
    public void compileFile(String filePath)
    {
        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();

        String[] args = {filePath, "-cp", filePath + File.pathSeparator + System.getProperty("java.class.path")};


        int exitCode = -1;
        try
        {
            exitCode = javaCompiler.run(null, null, null, args);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }

        if (exitCode != 0)
        {
            System.out.println("Compilation error occurred. Error code: " + exitCode);
        }
    }

    /**
     * Method that creates .jar file
     * @param sourceFile The location of package of java-class that need to be stored in archive.
     * @param jarPath The absolute path to java-class.
     * @throws IOException throws if jar file i/o stream error occurs
     */
    private void createJarFile(String sourceFile, Path jarPath) throws IOException
    {

        Manifest manifest = new Manifest();

        try (
                FileOutputStream fos = new FileOutputStream(jarPath.toFile());
                JarOutputStream jarOutputStream = new JarOutputStream(fos, manifest)
            )
        {
            File f = new File(jarPath.toUri());

            if (f.getParentFile() != null)
                f.getParentFile().mkdirs();

            if (f != null)
                f.createNewFile();
            else
            {
                System.out.println("invalid path to jar file to be created");
                System.exit(1);
            }

            String newClassPath = sourceFile.substring(sourceFile.indexOf("tmp/") + 4);

            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

            jarOutputStream.putNextEntry(new ZipEntry(newClassPath));

            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(sourceFile));

            int bytesRead;
            byte[] buffer = new byte[8 * 1024];
            while ((bytesRead = bis.read(buffer)) != -1)
            {
                jarOutputStream.write(buffer, 0, bytesRead);
            }

            jarOutputStream.closeEntry();
        }
        catch (IOException e)
        {
            throw new IOException(e.getMessage());
        }

    }

    protected void validator(Class<?> aClass) throws ImplerException {
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
    }

}



