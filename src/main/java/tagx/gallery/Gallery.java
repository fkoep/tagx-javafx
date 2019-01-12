package tagx.gallery;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public interface Gallery {
    public Entry getEntry(Path path);
    public void addEntry(Path path) throws IOException;
    public void removeEntry(Path path);
    public Stream<Entry> streamEntries();

    public void mergeJson(String json);
    public String toJson();
};
