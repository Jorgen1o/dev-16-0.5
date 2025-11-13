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
    @FXML private Button submitBtn;
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

    private boolean editMode = false;
    private String originalFullName = null;

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
    private void addStudent() {
        // Basic validation
        if (fullNameField.getText() == null || fullNameField.getText().isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Full Name is required.", ButtonType.OK).showAndWait();
            return;
        }
        if (whitelistCheckBox.isSelected() && blacklistCheckBox.isSelected()) {
            new Alert(Alert.AlertType.WARNING, "Whitelist and Blacklist are mutually exclusive.", ButtonType.OK).showAndWait();
            return;
        }

        Student s = buildFromForm();

        try {
            if (editMode) {
                // rename-safe update (see StudentStorage change below)
                StudentStorage.updateStudent(originalFullName, s);
            } else {
                // append new row
                StudentStorage.appendRow(new String[]{
                        s.getFullName(),
                        s.getAcademicStatus(),
                        s.getEmployed(),
                        s.getJobDetails(),
                        s.getProgrammingLanguages(),
                        s.getDatabases(),
                        s.getPreferredRole(),
                        s.getFacultyComment(),
                        s.getWhiteListed(),
                        s.getBlackListed()
                });
            }
            new Alert(Alert.AlertType.INFORMATION, editMode ? "Changes saved." : "Student added.", ButtonType.OK).showAndWait();
            if (!editMode) clearForm();  // keep edit fields on screen
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to save: " + e.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    private Student buildFromForm() {
        Student s = new Student();
        s.setFullName(fullNameField.getText().trim());
        s.setAcademicStatus(valueOf(academicStatusCombo));
        s.setEmployed(employedCheckBox.isSelected() ? "Yes" : "No");
        s.setJobDetails(textOf(jobDetailsField));
        s.setProgrammingLanguages(joinSelected(languagesList));
        s.setDatabases(joinSelected(databasesList));
        s.setPreferredRole(valueOf(preferredRoleCombo));
        s.setFacultyComment(textOf(commentsArea));
        s.setWhiteListed(whitelistCheckBox.isSelected() ? "Yes" : "No");
        s.setBlackListed(blacklistCheckBox.isSelected() ? "Yes" : "No");
        return s;
    }

    private String valueOf(ComboBox<String> cb) {
        return cb != null && cb.getValue() != null ? cb.getValue() : "";
    }
    private String textOf(TextInputControl t) {
        return t != null && t.getText() != null ? t.getText().trim() : "";
    }
    private String joinSelected(ListView<String> lv) {
        if (lv == null) return "";
        return String.join(", ", lv.getSelectionModel().getSelectedItems());
    }


    @FXML
    protected void goBack(javafx.event.ActionEvent event) throws IOException {
        String fxml;
        String title;

        if (editMode) {
            // Came from ViewStudents page
            fxml = "/cs151/application/view-students.fxml";
            title = "View Student Profiles";
        } else {
            // Came from Main Menu
            fxml = "/cs151/application/hello-view.fxml";
            title = "Home";
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        Scene scene = new Scene(loader.load(), 900, 600); // adjust size if needed

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();
    }



    public void editExistingStudent(Student s) {
        editMode = true;
        originalFullName = s.getFullName();

        if (submitBtn != null) submitBtn.setText("Save Changes");

        if (fullNameField != null) fullNameField.setText(s.getFullName());
        if (academicStatusCombo != null) academicStatusCombo.getSelectionModel().select(s.getAcademicStatus());
        if (employedCheckBox != null) employedCheckBox.setSelected(parseYes(s.getEmployed()));
        if (jobDetailsField != null) jobDetailsField.setText(s.getJobDetails());
        if (preferredRoleCombo != null) preferredRoleCombo.getSelectionModel().select(s.getPreferredRole());
        if (commentsArea != null) commentsArea.setText(s.getFacultyComment());
        if (whitelistCheckBox != null) whitelistCheckBox.setSelected(parseYes(s.getWhiteListed()));
        if (blacklistCheckBox != null) blacklistCheckBox.setSelected(parseYes(s.getBlackListed()));

        // select multiple values by splitting CSV-like strings
        selectListValues(languagesList, s.getProgrammingLanguages());
        selectListValues(databasesList, s.getDatabases());
    }

    private boolean parseYes(String v) {
        if (v == null) return false;
        String t = v.trim().toLowerCase();
        return t.equals("yes") || t.equals("true") || t.equals("y") || t.equals("1");
    }

    private void selectListValues(ListView<String> lv, String csv) {
        if (lv == null || csv == null) return;
        var items = lv.getItems();
        var toSelect = java.util.Arrays.stream(csv.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toList();
        for (int i = 0; i < items.size(); i++) {
            if (toSelect.contains(items.get(i))) {
                lv.getSelectionModel().select(i);
            }
        }
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