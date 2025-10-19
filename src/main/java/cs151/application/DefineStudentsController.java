package cs151.application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class DefineStudentsController {

    // fx:id MUST match define-students.fxml
    @FXML private TextField fullNameField;
    @FXML private ComboBox<String> academicStatusCombo;
    @FXML private CheckBox employedCheckBox;
    @FXML private TextField jobDetailsField;
    @FXML private ListView<String> languagesList;
    @FXML private ListView<String> databasesList;
    @FXML private ComboBox<String> preferredRoleCombo;

    @FXML
    public void initialize() {
        // In DefineStudentsController.initialize()
        languagesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        try (var br = new java.io.BufferedReader(
                new java.io.FileReader("ProgrammingLanguage.csv", java.nio.charset.StandardCharsets.UTF_8))) {
            String line; boolean header = true;
            while ((line = br.readLine()) != null) {
                if (header) { header = false; continue; }
                if (!line.isBlank()) languagesList.getItems().add(line.trim());
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    /** Called by Add button (onAction="#addStudent") */
    @FXML
    protected void addStudent() {
        try {
            String name = text(fullNameField);
            if (name.isBlank()) {
                alert(Alert.AlertType.WARNING, "Missing Name", "Please enter the student's name.");
                return;
            }
            String academic = combo(academicStatusCombo);
            String employed = employedCheckBox != null && employedCheckBox.isSelected() ? "Yes" : "No";
            String job = text(jobDetailsField);
            String langs = selected(languagesList);
            String dbs = selected(databasesList);
            String role = combo(preferredRoleCombo);

            StudentStorage.appendRow(new String[]{name, academic, employed, job, langs, dbs, role});
            alert(Alert.AlertType.INFORMATION, "Saved", "Student added successfully!");
            clearForm();
        } catch (Exception e) {
            e.printStackTrace();
            alert(Alert.AlertType.ERROR, "Error", "Unable to save: " + e.getMessage());
        }
    }

    @FXML
    protected void goBack(javafx.event.ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/cs151/application/hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 500);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Home");
        stage.show();
    }

    /* ---- helpers ---- */
    private static String text(TextField f) { return f == null ? "" : f.getText().trim(); }
    private static String combo(ComboBox<String> c) { return c == null || c.getValue() == null ? "" : c.getValue().trim(); }
    private static String selected(ListView<String> lv) {
        return lv == null ? "" : String.join(";", lv.getSelectionModel().getSelectedItems());
    }
    private static void alert(Alert.AlertType t, String h, String m) {
        Alert a = new Alert(t); a.setHeaderText(h); a.setContentText(m); a.showAndWait();
    }
    private void clearForm() {
        if (fullNameField != null) fullNameField.clear();
        if (academicStatusCombo != null) academicStatusCombo.getSelectionModel().clearSelection();
        if (employedCheckBox != null) employedCheckBox.setSelected(false);
        if (jobDetailsField != null) jobDetailsField.clear();
        if (languagesList != null) languagesList.getSelectionModel().clearSelection();
        if (databasesList != null) databasesList.getSelectionModel().clearSelection();
        if (preferredRoleCombo != null) preferredRoleCombo.getSelectionModel().clearSelection();
    }
}