package tagx.gallery;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.JsonReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;

// We encode paths in a platform-independent fashion by splitting them up into
// their parts and saving them as arrays.
//
// For example:
//
// Unix:
// foo/bar/xxx.png => {"foo", "bar", "xxx.png" }
//
// Windows:
// foo\bar\xxx.png => {"foo", "bar", "xxx.png" }
//
public class PathAdapter extends TypeAdapter<Path> {
    public PathAdapter(){}

    public Path read(JsonReader reader) throws IOException {
        Gson gson = new Gson();
        List<String> parts = gson.fromJson(reader, new ArrayList<String>().getClass());
        return Paths.get(String.join(File.separator, parts));
    }
    public void write(JsonWriter writer, Path path) throws IOException {
        Gson gson = new Gson();
        List<String> parts = new ArrayList<String>();
        for (Path p : path){
            parts.add(p.toString());
        }
        gson.toJson(parts, new ArrayList<String>().getClass(), writer);
    }
}
