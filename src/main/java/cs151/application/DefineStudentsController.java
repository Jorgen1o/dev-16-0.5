package cs151.application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.ArrayList;

import static cs151.application.AppFiles.loadLanguages;

public class DefineStudentsController {

    // fx:id MUST match define-students.fxml
    @FXML private TextField fullNameField;
    @FXML private ComboBox<String> academicStatusCombo;
    @FXML private CheckBox employedCheckBox;
    @FXML private TextField jobDetailsField;
    @FXML private ListView<String> languagesList;
    @FXML private ListView<String> databasesList;
    @FXML private ComboBox<String> preferredRoleCombo;
    @FXML private TextArea commentsArea;
    @FXML private CheckBox whitelistCheckBox;
    @FXML private CheckBox blacklistCheckBox;

    private static final String ERR_STYLE =
            "-fx-border-color:#d32f2f; -fx-border-width:1; -fx-border-radius:4;";

    @FXML
    public void initialize() {
        // In DefineStudentsController.initialize()
        languagesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        databasesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        whitelistCheckBox.selectedProperty().addListener((obs, was, is) -> {
            if (is) blacklistCheckBox.setSelected(false);
        });
        blacklistCheckBox.selectedProperty().addListener((obs, was, is) -> {
            if (is) whitelistCheckBox.setSelected(false);
        });

        try {
            loadLanguages(languagesList);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (jobDetailsField != null && employedCheckBox != null) {
            jobDetailsField.disableProperty().bind(employedCheckBox.selectedProperty().not());

            // Optional: clear any stale text when toggled off
            employedCheckBox.selectedProperty().addListener((obs, was, isNow) -> {
                if (!isNow && jobDetailsField.getText() != null && !jobDetailsField.getText().isBlank()) {
                    jobDetailsField.clear();
                }
            });
        }

        if (languagesList != null) {
            languagesList.getSelectionModel().selectedIndexProperty()
                    .addListener((o, a, b) -> clearError(languagesList));
        }
        if (databasesList != null) {
            databasesList.getSelectionModel().selectedIndexProperty()
                    .addListener((o, a, b) -> clearError(databasesList));
        }
        if (preferredRoleCombo != null) {
            preferredRoleCombo.valueProperty()
                    .addListener((o, a, b) -> clearError(preferredRoleCombo));
            preferredRoleCombo.setPromptText("Select a preferred role");
        }
        if (academicStatusCombo != null) {
            academicStatusCombo.valueProperty().addListener((o, a, b) -> clearError(academicStatusCombo));
            academicStatusCombo.setPromptText("Select academic status");
        }

        try (var br = new java.io.BufferedReader(
                new java.io.FileReader("ProgrammingLanguage.csv", java.nio.charset.StandardCharsets.UTF_8))) {
            String line; boolean header = true;
            while ((line = br.readLine()) != null) {
                if (header) { header = false; continue; }
                if (!line.isBlank()) languagesList.getItems().add(line.trim());
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void markError(Control c) { if (c != null) c.setStyle(ERR_STYLE); }
    private void clearError(Control c) { if (c != null) c.setStyle(""); }

    private boolean validateRequiredSelections() {
        var missing = new ArrayList<String>();

        boolean langsOk = languagesList != null &&
                !languagesList.getSelectionModel().getSelectedItems().isEmpty();
        boolean dbsOk = databasesList != null &&
                !databasesList.getSelectionModel().getSelectedItems().isEmpty();
        String role = preferredRoleCombo != null && preferredRoleCombo.getValue() != null
                ? preferredRoleCombo.getValue().trim() : "";
        boolean roleOk = !role.isBlank();
        String academic = (academicStatusCombo != null && academicStatusCombo.getValue() != null)
                ? academicStatusCombo.getValue().trim() : "";
        boolean academicOk = !academic.isBlank();

        if (!langsOk) missing.add("• Programming Languages");
        if (!dbsOk)   missing.add("• Databases");
        if (!roleOk)  missing.add("• Preferred Role");
        if (!academicOk)missing.add("• Academic Status");

        // highlight fields
        if (!langsOk) markError(languagesList); else clearError(languagesList);
        if (!dbsOk)   markError(databasesList);  else clearError(databasesList);
        if (!roleOk)  markError(preferredRoleCombo); else clearError(preferredRoleCombo);
        if (!academicOk) markError(academicStatusCombo); else clearError(academicStatusCombo);

        if (!missing.isEmpty()) {
            String msg = "Please select or fill the following before saving:\n\n"
                    + String.join("\n", missing);
            new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();

            // focus the first missing control
            if (!langsOk)      languagesList.requestFocus();
            else if (!dbsOk)   databasesList.requestFocus();
            else if (!roleOk)    preferredRoleCombo.requestFocus();
            else                 academicStatusCombo.requestFocus();
            return false;
        }
        return true;
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

            if (!validateRequiredSelections()) return;
            String langs = String.join(", ", languagesList.getSelectionModel().getSelectedItems());
            String dbs = String.join(", ", databasesList.getSelectionModel().getSelectedItems());
            String role = preferredRoleCombo.getValue().trim();
            String initialComment = (commentsArea == null || commentsArea.getText() == null)
                    ? "" : commentsArea.getText().trim();
            boolean white = whitelistCheckBox.isSelected();
            boolean black = blacklistCheckBox.isSelected();

            if (employedCheckBox != null && employedCheckBox.isSelected()) {
                if (jobDetailsField == null || jobDetailsField.getText() == null || jobDetailsField.getText().trim().isEmpty()) {
                    alert(Alert.AlertType.WARNING,
                            "Missing Job Details",
                            "Job Details is required when Employed is checked.");
                    if (jobDetailsField != null) jobDetailsField.requestFocus();
                    return; // stop; do not save until filled
                }
            } else {
                // If not employed, make sure we don't accidentally save stale details
                if (jobDetailsField != null) jobDetailsField.clear();
            }

            StudentStorage.appendRow(new String[]{name, academic, employed, job, langs, dbs, role, initialComment, String.valueOf(white), String.valueOf(black)});
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
        if (commentsArea != null) commentsArea.clear();
        if (whitelistCheckBox != null) whitelistCheckBox.setSelected(false);
        if (blacklistCheckBox != null) blacklistCheckBox.setSelected(false);
    }
}