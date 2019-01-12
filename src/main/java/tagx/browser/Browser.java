package tagx.browser;

import tagx.gallery.*;
import org.apache.commons.lang3.StringUtils;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener.Change;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.event.ActionEvent;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import java.nio.file.OpenOption;
import java.nio.charset.StandardCharsets;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.*;
import java.util.*;

public class Browser extends Application {
    private Gallery gallery = new SimpleGallery();
    
    private Parent root;
    private Scene scene;
    private Stage stage;

    @FXML
    private TextField searchField;
    // TODO when refreshing, use this instead of searchField
    // private List<String> searchQuery;
    private List<Entry> searchResults = new ArrayList<>();

    @FXML
    private ListView<String> contentView;
    private ObservableList<String> contentEntries;
    private ObservableList<String> contentEntriesSelected;

    @FXML
    private ListView<String> filterView;
    private ObservableList<String> filterTags;
    private ObservableList<String> filterTagsSelected;

    @FXML
    private ListView<String> detailView;
    private ObservableList<String> detailTags;
    private ObservableList<String> detailTagsSelected;

    private static Set<String> collectTags(Stream<Entry> entries){
        Set<String> tags = new TreeSet<>();
        entries.forEach(e -> e.streamTags().forEach(tags::add));
        return tags;
    }

    private Stream<Entry> toEntryStream(Stream<String> paths){
        return paths.map(s -> gallery.getEntry(Paths.get(s)));
    }

    @FXML
    public void onNewGallery(){
        gallery = new SimpleGallery();
        onSearch();
    }

    @FXML
    public void onMergeGallery(){
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Gallery");
        File file = chooser.showOpenDialog(stage);
        if (file == null) return;
        try {
            String json = String.join("\n", Files.readAllLines(file.toPath(), StandardCharsets.UTF_8));
            gallery.mergeJson(json);
        } catch (IOException e) {
            // TODO
            e.printStackTrace(System.out);
        }
        onSearch();
    }

    @FXML
    public void onLoadGallery(){
        onNewGallery();
        onMergeGallery();
    }

    @FXML
    public void onSaveGallery(){
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Destination");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("TagX files (*.tagx)", "*.tagx"));
        File file = chooser.showSaveDialog(stage);
        if (file == null) return;

        try {
            Files.write(file.toPath(), gallery.toJson().getBytes(), CREATE, TRUNCATE_EXISTING);
        } catch (IOException e) {
            // TODO
            e.printStackTrace(System.out);
        }
    }

    void refreshContentView(){
        // TODO restore selection?
        contentEntries.setAll(searchResults.stream()
                .filter(e -> filterTagsSelected.stream().allMatch(e::hasTag))
                .map(Entry::getPath)
                .map(Path::toString)
                .collect(Collectors.toList()));
    }
    void refreshFilterView(){
        // TODO restore selection?
        filterTags.setAll(collectTags(toEntryStream(contentEntries.stream())));
    }
    void refreshDetailView(){
        // TODO restore selection?
        // List<String> selected = detailTagsSelected.stream().collect(Collectors.toList());
        detailTags.setAll(collectTags(toEntryStream(contentEntriesSelected.stream())));
    }
    void refreshAll(){
        refreshContentView();
        refreshFilterView();
        refreshDetailView();
    }

    @FXML
    public void onSearch(){
        String text = searchField.getText();
        String[] query = text.trim().split("\\s+");
        searchResults = gallery.streamEntries()
                .filter(e -> (query.length == 0) || Stream.of(query).allMatch(q -> StringUtils.contains(e.getName(), q)))
                .collect(Collectors.toList());
        refreshAll();
    }

    @FXML
    public void onFilterSelect(){
        refreshContentView();
        refreshDetailView();
    }

    @FXML
    public void onFilterSelectAll(){
        filterView.getSelectionModel().selectAll();
    }

    @FXML
    public void onFilterSelectNone(){
        filterView.getSelectionModel().clearSelection();
    }

    @FXML
    public void onContentSelect(){
        refreshDetailView();
    }

    @FXML
    public void onAddEntries(){
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Files");
        List<File> files = chooser.showOpenMultipleDialog(stage);
        for (File file : files) {
            try {
                gallery.addEntry(file.toPath());
                onSearch();
            } catch (IOException e) {
                // TODO
                e.printStackTrace(System.out);
            }
        }
        onSearch();
    }

    @FXML
    public void onRemoveEntries(){
        contentEntriesSelected.stream()
            .map(Paths::get)
            .forEach(gallery::removeEntry);
        onSearch();
    }

    @FXML
    public void onAddTag(){
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Tag");
        if (dialog.showAndWait().isPresent()) {
            toEntryStream(contentEntriesSelected.stream())
                .forEach(e -> e.addTag(dialog.getEditor().getText()));
            refreshFilterView();
            refreshDetailView();
        }
    }

    @FXML
    public void onDeleteTags(){
        toEntryStream(contentEntriesSelected.stream())
            .forEach(e -> detailTagsSelected.stream()
                    .forEach(t -> e.removeTag(t)));
        refreshFilterView();
        refreshDetailView();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/browser.fxml"));
        loader.setController(this);

        this.root = loader.load();
        this.scene = new Scene(root);
        this.stage = stage;

        this.contentEntries = contentView.getItems();
        this.contentEntriesSelected = contentView.getSelectionModel().getSelectedItems();

        this.filterTags = filterView.getItems();
        this.filterTagsSelected = filterView.getSelectionModel().getSelectedItems();

        this.detailTags = detailView.getItems();
        this.detailTagsSelected = detailView.getSelectionModel().getSelectedItems();

        contentView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        filterView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        detailView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        filterTagsSelected.addListener((Change<? extends String> c) -> {
            onFilterSelect();
        });

        contentEntriesSelected.addListener((Change<? extends String> c) -> {
            onContentSelect();
        });

        stage.setTitle("tagX");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
