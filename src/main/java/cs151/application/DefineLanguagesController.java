package cs151.application;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DefineLanguagesController {

    @FXML private TextField languageField;     // matches FXML
    @FXML private ListView<String> languagesList;
    @FXML private Label errorLabel;

    private static final String LANG_CSV = "ProgrammingLanguage.csv"; // project root

    @FXML
    public void initialize() {
        languagesList.setItems(FXCollections.observableArrayList(loadLanguages()));
        if (errorLabel != null) errorLabel.setText("");
    }

    /** Add & autosave to CSV */
    @FXML
    private void addLanguage() {
        String name = languageField.getText() == null ? "" : languageField.getText().trim();
        if (name.isEmpty()) {
            setError("Please enter a language name.");
            return;
        }
        // prevent duplicates (case-insensitive)
        for (String s : languagesList.getItems()) {
            if (s.equalsIgnoreCase(name)) {
                setError("That language already exists.");
                return;
            }
        }
        // append to CSV immediately
        File f = new File(LANG_CSV);
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(f, true), StandardCharsets.UTF_8))) {
            if (!f.exists() || f.length() == 0) {
                pw.println("Name"); // header once
            }
            pw.println(name);
        } catch (IOException e) {
            setError("Failed to save: " + e.getMessage());
            return;
        }
        // update UI
        languagesList.getItems().add(name);
        languageField.clear();
        setError(""); // clear any previous error
    }

    /** Clear the TextField */
    @FXML
    private void onClear() {
        if (languageField != null) languageField.clear();
        setError("");
    }

    /** Delete selected item and rewrite the CSV */
    @FXML
    private void onDelete() {
        String sel = languagesList.getSelectionModel().getSelectedItem();
        if (sel == null) {
            setError("Select a language to delete.");
            return;
        }
        languagesList.getItems().remove(sel);
        writeAllLanguages(languagesList.getItems());
        setError("");
    }

    /** Back to home */
    @FXML
    private void goBack(javafx.event.ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/cs151/application/hello-view.fxml"));
        Scene scene = new Scene(loader.load(), 800, 500);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Home");
        stage.show();
    }

    /* ---------- helpers ---------- */

    private List<String> loadLanguages() {
        List<String> list = new ArrayList<>();
        File f = new File(LANG_CSV);
        if (!f.exists()) return list;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(f), StandardCharsets.UTF_8))) {
            String line; boolean header = true;
            while ((line = br.readLine()) != null) {
                if (header) { header = false; continue; }
                line = line.trim();
                if (!line.isBlank()) list.add(line);
            }
        } catch (IOException e) {
            setError("Failed to load CSV: " + e.getMessage());
        }
        return list;
    }

    private void writeAllLanguages(List<String> langs) {
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(LANG_CSV, false), StandardCharsets.UTF_8))) {
            pw.println("Name");
            for (String s : langs) pw.println(s);
        } catch (IOException e) {
            setError("Failed to rewrite CSV: " + e.getMessage());
        }
    }

    private void setError(String msg) {
        if (errorLabel != null) errorLabel.setText(msg == null ? "" : msg);
    }
}