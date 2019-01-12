package tagx.gallery;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.InvalidPathException;
import java.util.stream.Stream;
import java.util.*;

public class SimpleEntry implements Entry {
    private Path path;
    private String name;
    private Set<String> tags = new TreeSet<>();

    // TODO handle SecurityException?
    public SimpleEntry(Path path) throws IOException {
        this.path = path.toRealPath(); 
        this.name = this.path.getFileName().toString();
    }

    public Path getPath(){
        return path;
    }
    public String getName(){
        return name;
    }

    public boolean hasTag(String tag){
        return tags.contains(tag);
    }
    public void addTag(String tag){
        tags.add(tag);
    }
    public boolean removeTag(String tag){
        return tags.remove(tag);
    }
    public Stream<String> streamTags(){
        return tags.stream();
    }
};
