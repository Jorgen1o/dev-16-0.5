package cs151.application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class DefineLanguagesController {

    public TextField fullNameField;
    //References to text field where user enters new language
    @FXML private TextField nameField;
    @FXML private Label errorLabel;

    //Reference to the ListView that displays list of added programming language
    @FXML private ListView<String> languagesList;


    private ObservableList<String> items = FXCollections.observableArrayList();
    private final Set<String> canonical = new HashSet<>(); // lowercased, trimmed for dup checks

    private static final Path CSV_PATH = Paths.get("ProgrammingLanguage.csv");

    @FXML
    public void initialize() {
        items = FXCollections.observableArrayList();
        languagesList.setItems(items);
        loadIfPresent(); // harmless if file absent
    }

    @FXML
    private void onAdd() {
        errorLabel.setText("");
        String raw = nameField.getText();
        String trimmed = raw == null ? "" : raw.trim();

        // required field per Problem Statement §2.1.1(1)
        if (trimmed.isEmpty()) {
            errorLabel.setText("Language name required.");
            return;
        }

        // Check duplicates case-insensitively
        String key = trimmed.toLowerCase();
        if (canonical.contains(key)) {
            errorLabel.setText("Language already exists.");
            return;
        }

        // add and keep list sorted for a nicer UX
        canonical.add(key);
        items.add(trimmed);
        items.sort(String.CASE_INSENSITIVE_ORDER);
        nameField.clear();
    }

    @FXML
    private void onClear() {
        errorLabel.setText("");
        nameField.clear();
        languagesList.getSelectionModel().clearSelection();
    }

    @FXML
    private void onSave() {
        errorLabel.setText("");
        try {
            saveToCsv(items);
            errorLabel.setText("Saved to ProgrammingLanguage.csv");
        } catch (IOException e) {
            errorLabel.setText("Error saving CSV: " + e.getMessage());
        }
    }

    private void saveToCsv(ObservableList<String> data) throws IOException {
        // write header + sorted unique values
        Files.createDirectories(CSV_PATH.getParent() == null ? Paths.get(".") : CSV_PATH.getParent());
        try (BufferedWriter w = Files.newBufferedWriter(CSV_PATH, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            w.write("Name");
            w.newLine();
            for (String s : data.stream().distinct().sorted().toList()) {
                w.write(s);
                w.newLine();
            }
        }
    }

    private void loadIfPresent() {
        try {
            if (Files.exists(CSV_PATH)) {
                var lines = Files.readAllLines(CSV_PATH, StandardCharsets.UTF_8);
                for (String line : lines.stream().skip(1).toList()) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty()) {
                        String key = trimmed.toLowerCase();
                        if (canonical.add(key)) items.add(trimmed);
                    }
                }
                // ✅ Add this:
                items.sort(String.CASE_INSENSITIVE_ORDER);
            }
        } catch (IOException ignored) {
            // No problem if loading fails
        }
    }

    //handles action of going back to home screen
    @FXML
    protected void goBack(javafx.event.ActionEvent event) throws IOException {
        //Loads FXML for home screen
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/cs151/application/hello-view.fxml"));
        //Creates new scene with loaded FXML
        Scene scene = new Scene(fxmlLoader.load(), 800, 500);
        //Gets currents stage from event source
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        //Sets new scene
        stage.setScene(scene);
        stage.setTitle("Home");
        stage.show();
    }

    @FXML
    private void onDelete() {
        errorLabel.setText("");

        String selected = languagesList.getSelectionModel().getSelectedItem();

        if (selected == null) {
            errorLabel.setText("Please select a language to delete.");
            return;
        }

        // Remove from both lists
        items.remove(selected);
        canonical.remove(selected.toLowerCase());

        // Update CSV after deletion
        try {
            saveToCsv(items);
            errorLabel.setText("Deleted and saved changes.");
        } catch (IOException e) {
            errorLabel.setText("Error saving after deletion: " + e.getMessage());
        }
    }
}