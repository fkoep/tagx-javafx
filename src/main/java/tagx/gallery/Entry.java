package tagx.gallery;
import java.nio.file.Path;
import java.io.File;
import java.util.stream.Stream;

public interface Entry {
    public Path getPath();
    public String getName();

    public boolean hasTag(String name);
    public void addTag(String name);
    public boolean removeTag(String name);
    public Stream<String> streamTags();
};
