package cs151.application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SearchStudentsController {

    @FXML private TextField nameField;
    @FXML private TextField statusField;
    @FXML private TextField langField;
    @FXML private TextField dbField;
    @FXML private TextField roleField;

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

    private ObservableList<Student> fullList;

    @FXML
    public void initialize() {
        nameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getFullName()));
        academicStatusCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getAcademicStatus()));
        employedCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEmployed()));
        jobCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getJobDetails()));
        languagesCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getProgrammingLanguages()));
        databasesCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDatabases()));
        roleCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPreferredRole()));
        facultyComment.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getFacultyComment()));
        whiteListed.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getWhiteListed()));
        blackListed.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getBlackListed()));

        // ✅ Show multiline comments in table
        facultyComment.setCellFactory(col -> new TableCell<>() {
            private final Text text = new Text();
            {
                text.wrappingWidthProperty().bind(col.widthProperty().subtract(16));
                setGraphic(text);
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                text.setText(empty ? null : item);
            }
        });

        studentsTable.setFixedCellSize(-1);

        refreshList();
    }

    private void refreshList() {
        try {
            List<Student> list = new ArrayList<>();
            for (String[] r : StudentStorage.readAllRows()) {
                if (r.length >= 10) {
                    list.add(new Student(
                            r[0], r[1], r[2], r[3], r[4], r[5], r[6],
                            r[7], StudentStorage.toYesNo(r[8]), StudentStorage.toYesNo(r[9])
                    ));
                }
            }
            fullList = FXCollections.observableArrayList(list);
            studentsTable.setItems(fullList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void search() {
        String name = nameField.getText().toLowerCase().trim();
        String stat = statusField.getText().toLowerCase().trim();
        String lang = langField.getText().toLowerCase().trim();
        String db = dbField.getText().toLowerCase().trim();
        String role = roleField.getText().toLowerCase().trim();

        ObservableList<Student> filtered = FXCollections.observableArrayList();

        for (Student s : fullList) {
            if (contains(s.getFullName(), name) &&
                    contains(s.getAcademicStatus(), stat) &&
                    contains(s.getProgrammingLanguages(), lang) &&
                    contains(s.getDatabases(), db) &&
                    contains(s.getPreferredRole(), role)) {

                filtered.add(s);
            }
        }

        studentsTable.setItems(filtered);
    }

    private boolean contains(String value, String filter) {
        return filter.isEmpty() || (value != null && value.toLowerCase().contains(filter));
    }

    @FXML
    private void clearFilters() {
        nameField.clear();
        statusField.clear();
        langField.clear();
        dbField.clear();
        roleField.clear();
        studentsTable.setItems(fullList);
    }

    @FXML
    private void editSelected() {
        Student sel = studentsTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            alert("No selection", "Select a student to edit.");
            return;
        }
        alert("Edit", "Edit function not yet implemented on this page.");
    }

    @FXML
    private void addComment() {
        Student sel = studentsTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            alert("No selection", "Select a student to add a comment.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Comment");
        dialog.setHeaderText("Add a new comment for " + sel.getFullName());

        TextArea textArea = new TextArea();
        textArea.setPrefRowCount(5);
        dialog.getDialogPane().setContent(textArea);

        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn == saveBtn) {
                String comment = textArea.getText().trim();
                if (!comment.isEmpty()) {
                    String dated = LocalDate.now() + ": " + comment;
                    String existing = sel.getFacultyComment();

                    sel.setFacultyComment(
                            (existing == null || existing.isBlank())
                                    ? dated
                                    : existing + System.lineSeparator() + dated
                    );

                    try {
                        StudentStorage.updateStudent(sel);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    refreshList();
                }
            }
        });
    }

    @FXML
    private void deleteSelected() {
        Student sel = studentsTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            alert("No selection", "Select a student to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete profile for \"" + sel.getFullName() + "\"?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirm Deletion");

        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                try {
                    StudentStorage.deleteStudent(sel);
                    refreshList();
                } catch (Exception e) {
                    e.printStackTrace();
                    alert("Error", "Failed to delete student.");
                }
            }
        });
    }

    @FXML
    private void goBack(javafx.event.ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/cs151/application/hello-view.fxml"));
        Scene scene = new Scene(loader.load());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Home");
        stage.show();
    }

    private void alert(String header, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        a.setHeaderText(header);
        a.showAndWait();
    }
}
