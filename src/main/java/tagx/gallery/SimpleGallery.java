package tagx.gallery;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.*;
import java.util.*;

public class SimpleGallery implements Gallery {
    private Map<Path, Entry> entries = new TreeMap<>();

    public SimpleGallery(){}

    public Entry getEntry(Path path){
        return entries.get(path.toAbsolutePath());
    }
    public void addEntry(Path path) throws IOException {
        path = path.toRealPath();
        if (!entries.containsKey(path)) {
            entries.put(path, new SimpleEntry(path));
        }
    }
    public void removeEntry(Path path){
        entries.remove(path);
    }
    public Stream<Entry> streamEntries(){
        return entries.values().stream();
    }

    @SuppressWarnings("unchecked")
    public void mergeJson(String json){
        Gson gson = new Gson();

        try {
            Map<String, List<String>> data = gson.fromJson(json, new HashMap<String, ArrayList<String>>().getClass());
            for (Map.Entry<String, List<String>> pair : data.entrySet()) {
                Path path = Paths.get(pair.getKey());
                addEntry(path);
                Entry entry = getEntry(path);
                pair.getValue().stream().forEach(entry::addTag);
            }
        } catch (IOException e) {
            // TODO
            e.printStackTrace(System.out);
        }
    }

    @SuppressWarnings("unchecked")
    public String toJson(){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        Map<String, List<String>> data = streamEntries()
            .collect(Collectors.toMap(
                        e -> e.getPath().toString(),
                        e -> e.streamTags().collect(Collectors.toList())));

        return gson.toJson(data, data.getClass());
    }
};
