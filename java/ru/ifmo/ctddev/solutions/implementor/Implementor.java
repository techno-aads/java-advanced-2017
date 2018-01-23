package ru.ifmo.ctddev.solutions.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;


/**
 * Implenting classs for interfaces {@link Impler} and {@link JarImpler}
 *  @see JarImpler
 *
 * @author AST
 * @version 1.0
 * @since 1.0
 */

public class Implementor implements JarImpler {

    /**
     * Class Name.
     */
    private String ClassName;

    /**
     * Input class or interface.
     */
    private Class<?> ImplementedClass;

    /**
     * The writer class source in file.
     */
    private Writer FileWriter;

    /**
     * Runs Class-Implementor (implement) or Creates Jar(jarImplement).
     *
     * @param args The array of arguments for Implementor.
     *             2 types of using :
     *             <ul>
     *               <li> -jar fullClassName generatedFilesLocation.  </li>
     *               <li> fullClassName generatedFilesLocation.  </li>
     *             </ul>
     *
     */
    public static void main(String[] args)
    {
        System.out.println("Hello!");

        if (args.length < 2)
        {
            System.out.println("Not Enough args");
            System.exit(1);
        }

        if (args.length > 3)
        {
            System.out.println("Too much args");
            System.exit(1);
        }


        if ((args.length == 3)&&(!args[0].equals("-jar")))
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
            System.out.println("Not enough main() arguments");
        }
    }

    class UnicodeFilterWriter extends FilterWriter
    {
        UnicodeFilterWriter(Writer writer)
        {
            super(writer);
        }

        @Override
        public void write(int i) throws IOException
        {
            out.write(String.format("\\u%04X", i));
        }

    }

    /**
     * The class-wrapper for java methods. Provides meaningful methods {@link MethodWrapper#equals(Object)},
     * {@link MethodWrapper#hashCode()} and {@link MethodWrapper#toString()} and is
     * used to contain methods in {@link HashSet}.
     */
    class MethodWrapper
    {
        /**
         * The method, kept in this exemplar of {@link MethodWrapper}.
         */
        private final Method method;

        /**
         * Calculated hash for the method. Its value is stored during the first call of constructor.
         */
        private final int hash;

        /**
         * Creates new copy of the class and stores given method in it.
         * @param method input method.
         */
        MethodWrapper(Method method)
        {
            this.method = method;
            hash = (method.getName() + printArgList(method.getParameterTypes())).hashCode();
        }

        /**
         * Returns hash code of method stored in this object.
         * @return needed hash code.
         */
        @Override
        public int hashCode()
        {
            return hash;
        }

        /**
         * compare wrappers and input method .
         * @param o the given object.
         * @return <tt> true </tt>, if objects are equal, <tt>false </tt> otherwise.
         */
        @Override
        public boolean equals(Object o)
        {
            if (o == null || !(o instanceof MethodWrapper))
            {
                return false;
            }
            MethodWrapper temp = (MethodWrapper) o;
            return temp.method.getName().equals(method.getName()) && Arrays.equals(temp.method.getParameterTypes(), method.getParameterTypes());
        }

        /**
         * Generates the string presentation of mehtod in java source file.
         * @return generated string.
         */
        @Override
        public String toString()
        {
            return "\t" + printModifier(method.getModifiers())
                    + method.getReturnType().getTypeName() + " "
                    + method.getName()
                    + printArgList(method.getParameterTypes()) + "{\n"
                    + returnImpl(method.getReturnType()) + "\n"
                    + "\t" + "}\n";
        }
    }

    public void generateClassSource(Writer temp) throws IOException, ImplerException
    {
        generatePackage();
        generateHeader();
        generateConstructors();
        generateMethods();
    }

    /**
     * Implements from input Class or Interface.
     * The generated class will have suffix "Impl".
     *
     * @param _Class Class or Interface to implement.
     * @param _Path   The location for generated class.
     * @throws ImplerException {@link ImplerException} if the given class cannot be generated.
     */
    @Override
    public void implement(Class<?> _Class, Path _Path) throws ImplerException
    {
        if (_Class.isPrimitive() || _Class.isArray() || _Class == Enum.class)
        {
            throw new ImplerException("Input Class<> is not  class or interface");
        }

        if (Modifier.isFinal(_Class.getModifiers()))
        {
            throw new ImplerException("Can't implement final class");
        }

        ImplementedClass = _Class;
        ClassName = _Class.getSimpleName() + "Impl";
        Path path2ImplClass = null;

        try
        {
            path2ImplClass = createDirectory(_Path, _Class, ".java");
        }

        catch (IOException e)
        {
            System.out.println("Rrror Creating directory");
        }

        try (Writer temp = new UnicodeFilterWriter(Files.newBufferedWriter(path2ImplClass)))
        {
            FileWriter = temp;
            generateClassSource(temp);
            FileWriter.write("}\n");
        }
        catch (IOException e)
        {
            throw new ImplerException(e);
        }
    }

    /**
     * Generates the string <tt>"return x;"</tt>, where x is 0, false, null or empty string.
     * Chooses the variant to fit returning type of method.
     *
     * @param c The returning type of method.
     * @return Generated string.
     */
    private String returnImpl(Class<?> c)
    {
        StringBuilder t = new StringBuilder("\t" + "\t" + "return");
        if (!c.isPrimitive())
        {
            t.append(" null");
        }
        else if (c.equals(boolean.class))
        {
            t.append(" false");
        }
        else if (!c.equals(void.class))
        {
            t.append(" 0");
        }
        return t.append(";").toString();
    }

    /**
     * Implements all methods from class or interface ImplementedClass and public and protected methods of its superclasses.
     * @throws IOException if an error occured while writing to the destination file via. {@link Writer}
     */
    private void generateMethods() throws IOException
    {
        Set<MethodWrapper> methods = new HashSet<>();
        addMethods(methods, ImplementedClass.getMethods());

        while (ImplementedClass != null)
        {
            addMethods(methods, ImplementedClass.getDeclaredMethods());
            ImplementedClass = ImplementedClass.getSuperclass();
        }

        for (MethodWrapper method : methods)
        {
            FileWriter.write(method + "\n");
        }
    }

    /**
     * Saving to set of Methods
     *
     * @param set Set of methods where to save only Abstract
     * @param methods array of methods for save
     */
    private void addMethods(Set<MethodWrapper> set, Method[] methods)
    {
        for (Method method : methods)
        {
            if (Modifier.isAbstract(method.getModifiers()))
            {
                set.add(new MethodWrapper(method));
            }
        }
    }

    /**
     * Generates the string, with argument-list of method or constructor with names varName + i.
     *
     * @param argList The array of types of arguments.
     * @return The string, denoting arguments list.
     */
    private String printArgList(Class<?> argList[])
    {
        StringBuilder t = new StringBuilder("(");
        for (int i = 0; i < argList.length; i++)
        {
            t.append(argList[i].getTypeName()).append(" ").append("var").append(i);
            if (i != argList.length - 1)
            {
                t.append(", ");
            }
        }
        t.append(") ");
        return t.toString();
    }

    /**
     * Implements  constructors of the class/interface ImplementedClass.
     *
     * @throws ImplerException If ImplementedClass is class with no public constructors.
     * @throws IOException if an error occured while writing to the destination file via {@link Writer}.
     */
    private void generateConstructors() throws ImplerException, IOException
    {
        boolean flag = true;
        for (Constructor<?> constructor : ImplementedClass.getDeclaredConstructors())
        {
            if (!Modifier.isPrivate(constructor.getModifiers()))
            {
                flag = false;
                FileWriter.write("\t" + printModifier(ImplementedClass.getModifiers()));
                FileWriter.write(ClassName);
                FileWriter.write(printArgList(constructor.getParameterTypes()));
                FileWriter.write(exceptionsList(constructor.getExceptionTypes()));

                FileWriter.write("{\n" + "\t" + "\t" + "super(");
                for (int i = 0; i < constructor.getParameterCount(); i++)
                {
                    FileWriter.write("var" + i);
                    if (i != constructor.getParameterCount() - 1)
                    {
                        FileWriter.write(", ");
                    }
                }
                FileWriter.write(");\n" + "\t" + "}\n\n");
            }
        }
        if (!ImplementedClass.isInterface() && flag)
        {
            throw new ImplerException("Only private constructors");
        }
    }

    /**
     * Generates the string of exceptions from exceptions array.
     *
     * @param exceptionTypes The array of type of exceptions, thrown by constructor.
     * @return Generated String.
     */
    private String exceptionsList(Class<?>[] exceptionTypes)
    {
        if (exceptionTypes.length == 0) {
            return "";
        }
        StringBuilder t = new StringBuilder("throws ");
        for (int i = 0; i < exceptionTypes.length; i++)
        {
            t.append(exceptionTypes[i].getTypeName());
            if (i != exceptionTypes.length - 1)
            {
                t.append(",");
            }
            t.append(" ");
        }
        return t.toString();
    }

    /**
     * By given Class object creates directory for its java source or object file.
     *
     * @param _Path   The location, where packages directories are created.
     * @param c      The class object containing its packages data.
     * @param suffix File expansion. java-source or java-object file.
     * @return The relative _Path to given class file.
     * @throws IOException Directories are created using {@link Files#createDirectories(Path, FileAttribute[])}
     */
    private Path createDirectory(Path _Path, Class<?> c, String suffix) throws IOException
    {
        if (c.getPackage() != null)
        {
            _Path = _Path.resolve(c.getPackage().getName().replace(".", "/"));
        }
        Files.createDirectories(_Path);
        return _Path.resolve(ClassName + suffix);
    }

    /**
     * If the class ImplementedClass isn't located in default package, prints concatanation of string "package " and given class package name.
     * @throws IOException if an error occured while writing to the destination file via {@link Writer}.
     */
    private void generatePackage() throws IOException
    {
        if (ImplementedClass.getPackage() != null)
        {
            FileWriter.write("package " + ImplementedClass.getPackage().getName() + ";\n\n");
        }
    }

    /**
     * Prints header of generated ImplementedClass.
     * @throws IOException if an error occured while writing to the destination file via {@link Writer}.
     */
    private void generateHeader() throws IOException
    {
        FileWriter.write(printModifier(ImplementedClass.getModifiers()));
        FileWriter.write("class ");
        FileWriter.write(ClassName + " ");
        FileWriter.write(ImplementedClass.isInterface() ? "implements " : "extends ");
        FileWriter.write(ImplementedClass.getSimpleName() + " {\n");
    }

    /**
     * Generate string containig modifiers from the given int value {@link Modifier#ABSTRACT},
     * {@link Modifier#TRANSIENT}, {@link Modifier#INTERFACE}.
     *
     * @param modifiers the byte mask of the modifiers.
     * @return The string generated from given int.
     */
    private String printModifier(int modifiers)
    {
        return Modifier.toString(modifiers & ~(Modifier.ABSTRACT | Modifier.TRANSIENT |
                Modifier.INTERFACE)) + " ";
    }

    /**
     * Crates jar files with implemented files
     * @param token {@link Class} type token to create implementation for
     * @param jarFile {@link Path} path where generated file need save
     * @throws ImplerException if appeared exceptions during implementation
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        Path directoryPath;
        if (jarFile.toString().lastIndexOf(File.separator) != -1) {
            directoryPath = Paths.get(jarFile.toString().substring(0, jarFile.toString().lastIndexOf(File.separator)));
        }
        else{
            directoryPath = Paths.get("").toAbsolutePath();
        }
        implement(token, directoryPath);
        try {
            compileAndPackToJar(token, directoryPath, jarFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method which compiles and packs created implementation to Jar file
     * @param clazz {@link Class} class for which implementation generated
     * @param fileDirectory {@link Path} path to directory with generated files
     * @param jarFile {@link Path} path to generated jar file
     * @throws ImplerException when exceptions during compiling or saving to archive
     */
    private void compileAndPackToJar(Class clazz, Path fileDirectory, Path jarFile) throws ImplerException, IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        List<String> args = new ArrayList<>();
        args.add("-cp");
        args.add(System.getProperty("java.class.path"));
        args.add(getOutputFile(clazz, fileDirectory).toString());
        int exitCode = compiler.run(null, null, null, args.toArray(new String[args.size()]));
        if (exitCode != 0) throw new ImplerException("Compilation error");

        String packagePath = clazz.getPackage().getName().replace(".", File.separator);
        String classPath = packagePath + File.separator + clazz.getSimpleName() + "Impl.class";
        try (JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(jarFile))) {
            jarOutputStream.putNextEntry(new JarEntry(classPath));
            Files.copy(fileDirectory.resolve(classPath), jarOutputStream);
        } catch (IOException e) {
            throw new ImplerException("Packaging error");
        }
    }

    /**
     * Helper method to extract output file and additional data
     * @param clazz {@link Class} class for which need generate implementation
     * @param path {@link Path} path where generated file need save
     * @return {@link File} file for writer
     * @throws IOException if passed wrong parameters
     */
    private File getOutputFile(Class<?> clazz, Path path) throws IOException {
        String classFileName = clazz.getSimpleName() + "Impl.java";
        String[] packages = clazz.getPackage().getName().split("\\.");
        Path outputPath = Paths.get(path.toAbsolutePath().toString(), packages);
        outputPath = Paths.get(outputPath.toString(), classFileName);
        return outputPath.toFile();
    }

    /**
     * Remove the given file/directory and all in it.
     * @param _Root the _Path to the given file/directory.
     * default provider, the checkRead method is invoked to check read access to the directory.
     * @throws IOException If error occured while compiling or creating files.
     */
    private void clean(final Path _Root) throws IOException
    {
        if (!Files.exists(_Root))
        {
            return;
        }
        Files.walkFileTree(_Root, new SimpleFileVisitor<Path>()
        {
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException
            {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException
            {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Implements the given class in given directory, compiles it and stores its object file there.
     * @param _Class the given class.
     * @param _Path directory to store output files.
     * @return Path to created object file.
     * @throws ImplerException If error occured while compiling or creating files.
     * @throws IOException If error occured while compiling or creating files.
     * @see Implementor#implement(Class, Path)
     */
    private Path createAndCompile(Class<?> _Class, Path _Path) throws ImplerException, IOException
    {
        implement(_Class, _Path);
        JavaCompiler t = ToolProvider.getSystemJavaCompiler();

        if (t.run(null, null, null,
                createDirectory(_Path, _Class, ".java").toString(), "-cp",
                _Class.getPackage().getName() + File.pathSeparator
                        + System.getProperty("java.class._Path")) != 0) {
            throw new ImplerException("Can't compile the given class");
        }
        return createDirectory(_Path, _Class, ".class");
    }

    /**
     * Creates Jar file in the given directory and stores the given file in it.
     * @param _Dirs The location of package of java-class that need to be stored in archive.
     * @param _PathFile The absolute _Path to java-class.
     * @param _Path The directory, where jar file is stored.
     * @throws IOException If error occured while using {@link JarOutputStream}
     */
    private void createJarFile(Path _Dirs, Path _PathFile, Path _Path) throws IOException
    {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(_Path), manifest))
        {
            out.putNextEntry(new ZipEntry(_PathFile.toString().replace(File.separator, "/")));
            Files.copy(_Dirs.resolve(_PathFile), out);
        }
    }
}