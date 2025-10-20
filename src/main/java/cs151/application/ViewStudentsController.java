package cs151.application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

public class ViewStudentsController {

    // Table
    @FXML private TableView<Student> studentsTable;
    @FXML private TableColumn<Student, String> nameCol;
    @FXML private TableColumn<Student, String> academicStatusCol;
    @FXML private TableColumn<Student, String> employedCol;
    @FXML private TableColumn<Student, String> jobCol;
    @FXML private TableColumn<Student, String> languagesCol;
    @FXML private TableColumn<Student, String> databasesCol;
    @FXML private TableColumn<Student, String> roleCol;
    @FXML private TableColumn<Student, String> facultyComment;
    @FXML private TableColumn<Student, String> whiteListed;
    @FXML private TableColumn<Student, String> blackListed;

    private SortedList<Student> sorted;

    // Search Filter
    @FXML private TextField nameField, jobContains, langContains, dbContains;
    @FXML private ComboBox<String> academicStatusCombo, roleCombo;
    @FXML private CheckBox employedOnly;

    private final ObservableList<Student> master = FXCollections.observableArrayList();
    private FilteredList<Student> filtered;

    @FXML
    public void initialize() {
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        academicStatusCol.setCellValueFactory(new PropertyValueFactory<>("academicStatus"));
        employedCol.setCellValueFactory(new PropertyValueFactory<>("employed"));
        jobCol.setCellValueFactory(new PropertyValueFactory<>("jobDetails"));
        languagesCol.setCellValueFactory(new PropertyValueFactory<>("programmingLanguages"));
        databasesCol.setCellValueFactory(new PropertyValueFactory<>("databases"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("preferredRole"));
        facultyComment.setCellValueFactory(new PropertyValueFactory<>("facultyComment"));
        whiteListed.setCellValueFactory(new PropertyValueFactory<>("whiteListed"));
        blackListed.setCellValueFactory(new PropertyValueFactory<>("blackListed"));

        studentsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        filtered = new FilteredList<>(master, s -> true);
        sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(studentsTable.comparatorProperty());
        studentsTable.setItems(sorted);

        refresh();

        // Populate dropdowns
        academicStatusCombo.getItems().setAll("Freshman", "Sophomore", "Junior", "Senior", "Graduate");
        roleCombo.getItems().setAll("Backend Developer", "Frontend Developer", "Full Stack Developer", "Data Engineer");
        initFilterChoices();
        installPrompt(academicStatusCombo, "Academic status");
        installPrompt(roleCombo, "Preferred role");
    }

    private void refresh() {
        List<Student> loaded = loadStudents();
        master.setAll(loaded);

        if (master.isEmpty()) {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("No Students Found");
            a.setHeaderText("No Stored Student Profiles");
            a.setContentText("Add profiles in the Define Students screen, then return here.");
            a.showAndWait();
        }
    }

    @FXML
    public void applyFilters() {
        filtered.setPredicate(buildPredicate());
    }

    @FXML
    public void clearFilters() {
        if (nameField != null) nameField.clear();
        if (jobContains != null) jobContains.clear();
        if (langContains != null) langContains.clear();
        if (dbContains != null) dbContains.clear();
        if (academicStatusCombo != null) {
            academicStatusCombo.getSelectionModel().clearSelection();
            academicStatusCombo.setValue(null);        // explicitly no selection
        }
        if (roleCombo != null) {
            roleCombo.getSelectionModel().clearSelection();
            roleCombo.setValue(null);
        }
        if (employedOnly != null) employedOnly.setSelected(false);
        initFilterChoices();
        filtered.setPredicate(s -> true);
    }

    private Predicate<Student> buildPredicate() {
        String name = norm(nameField);
        String status = norm(academicStatusCombo);
        String role = norm(roleCombo);
        String job = norm(jobContains);
        String lang = norm(langContains);
        String db = norm(dbContains);
        boolean empOnly = employedOnly != null && employedOnly.isSelected();

        return s -> {
            if (!contains(norm(s.getFullName()), name)) return false;
            if (!status.isEmpty() && !eq(norm(s.getAcademicStatus()), status)) return false;
            if (empOnly && !"yes".equals(norm(s.getEmployed()))) return false;
            if (!job.isEmpty() && !contains(norm(s.getJobDetails()), job)) return false;
            if (!lang.isEmpty() && !contains(norm(s.getProgrammingLanguages()), lang)) return false;
            if (!db.isEmpty() && !contains(norm(s.getDatabases()), db)) return false;
            if (!role.isEmpty() && !eq(norm(s.getPreferredRole()), role)) return false;
            return true;
        };
    }

    private static String norm(TextField t) { return t == null || t.getText() == null ? "" : t.getText().trim().toLowerCase(); }
    private static String norm(ComboBox<String> c) { return (c == null || c.getValue() == null) ? "" : c.getValue().trim().toLowerCase(); }
    private static String norm(String s) { return s == null ? "" : s.trim().toLowerCase(); }
    private static boolean contains(String hay, String needle) { return needle.isEmpty() || hay.contains(needle); }
    private static boolean eq(String a, String b) { return a.equals(b); }

    // === Edit ===

    @FXML
    public void editSelected() {
        Student selected = studentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            FXMLLoader fxml = new FXMLLoader(getClass().getResource("/cs151/application/edit-student.fxml"));
            DialogPane pane = fxml.load();
            EditStudentController ctrl = fxml.getController();
            ctrl.setStudent(selected);

            Dialog<ButtonType> dlg = new Dialog<>();
            dlg.setTitle("Edit Student");
            dlg.setDialogPane(pane);
            Optional<ButtonType> res = dlg.showAndWait();
            if (res.isPresent() && res.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                Student updated = ctrl.buildStudent();
                // prevent duplicate name (other than self)
                if (wouldDuplicateName(updated.getFullName(), selected)) {
                    new Alert(Alert.AlertType.ERROR,
                            "Another profile already uses the full name \"" + updated.getFullName() + "\".")
                            .showAndWait();
                    return;
                }
                // replace in master list
                int idx = master.indexOf(selected);
                if (idx >= 0) master.set(idx, updated);
                persistAll(); // write back to CSV
            }
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to open editor: " + e.getMessage()).showAndWait();
        }
    }

    private void persistAll() {
        try {
            List<String[]> rows = new ArrayList<>();
            for (Student s : master) {
                rows.add(new String[] {
                        s.getFullName(), s.getAcademicStatus(), s.getEmployed(), s.getJobDetails(),
                        s.getProgrammingLanguages(), s.getDatabases(), s.getPreferredRole(),
                        s.getFacultyComment(), s.getWhiteListed(), s.getBlackListed()
                });
            }
            StudentStorage.writeAllRows(rows);
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to save: " + e.getMessage()).showAndWait();
        }
    }

    private boolean wouldDuplicateName(String candidate, Student self) {
        String key = norm(candidate);
        for (Student s : master) {
            if (s == self) continue;
            if (norm(s.getFullName()).equals(key)) return true;
        }
        return false;
    }

    private void initFilterChoices() {
        if (academicStatusCombo.getItems().isEmpty()) {
            academicStatusCombo.getItems().setAll("Freshman", "Sophomore", "Junior", "Senior", "Graduate");
        }
        if (roleCombo.getItems().isEmpty()) {
            roleCombo.getItems().setAll(
                    "Backend Developer", "Frontend Developer", "Full Stack Developer", "Data Engineer"
            );
        }
    }

    private void installPrompt(ComboBox<String> cb, String prompt) {
        cb.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null || item.isBlank()) ? prompt : item);
            }
        });
    }

    private List<Student> loadStudents() {
        List<Student> list = new ArrayList<>();
        try {
            for (String[] r : StudentStorage.readAllRows()) {
                if (r.length >= 10) list.add(new Student(r[0], r[1], r[2], r[3], r[4], r[5], r[6], r[7], StudentStorage.toYesNo(r[8]), StudentStorage.toYesNo(r[9])));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @FXML
    private void deleteSelected() {
        Student sel = studentsTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Select a row to delete.", ButtonType.OK);
            a.setHeaderText("No selection");
            a.showAndWait();
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete profile for \"" + sel.getFullName() + "\"?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirm deletion");
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                try {
                    StudentStorage.deleteStudent(sel);
                    refresh();
                } catch (Exception e) {
                    e.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, "Failed to delete: " + e.getMessage(), ButtonType.OK).showAndWait();
                }
            }
        });
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
}