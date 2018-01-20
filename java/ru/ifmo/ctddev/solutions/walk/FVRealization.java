import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class FVRealization extends SimpleFileVisitor<Path> {

    List<String> resString;

    FVRealization() {
        resString = new ArrayList<>();
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        int i = Walk.hashCodeForFile(file.toFile());
        String s = String.format("%08x %s", i, file.toString());
        resString.add(s);
        System.out.println(s);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path path, IOException exc) throws IOException {
        resString.add("00000000 " + path.toString());
        System.out.println("00000000 " + path.toString());
        return FileVisitResult.CONTINUE;
    }

    public List<String> getResString() {
        return resString;
    }

}