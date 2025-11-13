package cs151.application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ViewStudentsController {

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

    private static boolean parseBool(String s) {
        if (s == null) return false;
        String t = s.trim().toLowerCase();
        return t.equals("yes") || t.equals("true") || t.equals("y") || t.equals("1");
    }

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

        facultyComment.setCellFactory(col -> {
            return new TableCell<Student, String>() {
                private final javafx.scene.text.Text text = new javafx.scene.text.Text();
                {
                    text.wrappingWidthProperty().bind(col.widthProperty().subtract(16)); // padding
                    setGraphic(text);
                }
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        text.setText(null);
                    } else {
                        text.setText(item); // shows real \n as line breaks
                    }
                }
            };
        });

        // Allow row height to auto-resize for multiline text
        studentsTable.setFixedCellSize(-1);

        refresh();
    }

    private void refresh() {
        ObservableList<Student> data = FXCollections.observableArrayList(loadStudents());
        if (data.isEmpty()) {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("No Students Found");
            a.setHeaderText("No Stored Student Profiles");
            a.setContentText("Add profiles in the Define Students screen, then return here.");
            a.showAndWait();
        }
        sorted = new SortedList<>(data);
        sorted.setComparator(Comparator.comparing(s ->
                Optional.ofNullable(s.getFullName()).orElse("").toLowerCase()));
        studentsTable.setItems(sorted);
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
    private void addComment() {
        Student sel = studentsTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Select a student to add a comment.", ButtonType.OK);
            a.setHeaderText("No selection");
            a.showAndWait();
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Comment");
        dialog.setHeaderText("Add new comment for " + sel.getFullName());
        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        TextArea ta = new TextArea();
        ta.setPromptText("Type your comment...");
        ta.setWrapText(true);
        ta.setPrefRowCount(6);
        dialog.getDialogPane().setContent(ta);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn == saveBtn) {
                String comment = ta.getText().trim();
                if (!comment.isEmpty()) {
                    String datedComment = java.time.LocalDate.now() + ": " + comment;

                    // append with a real newline (use OS line separator)
                    String existing = sel.getFacultyComment();
                    String sep = System.lineSeparator();
                    if (existing == null || existing.isBlank()) {
                        sel.setFacultyComment(datedComment);
                    } else {
                        sel.setFacultyComment(existing + sep + datedComment);
                    }

                    try {
                        StudentStorage.updateStudent(sel.getFullName(), sel);
                    } catch (Exception e) {
                        e.printStackTrace();
                        new Alert(Alert.AlertType.ERROR, "Failed to save comment.", ButtonType.OK).showAndWait();
                    }

                    studentsTable.refresh();
                }
            }
        });
    }

    @FXML
    private void editSelected(javafx.event.ActionEvent event) throws IOException {
        Student sel = studentsTable.getSelectionModel().getSelectedItem();

        if (sel == null) {
            Alert a = new Alert(
                    Alert.AlertType.WARNING,
                    "Select a row to edit.",
                    ButtonType.OK
            );
            a.setHeaderText("No selection");
            a.showAndWait();
            return;
        }

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/cs151/application/define-students.fxml")
        );

        Scene scene = new Scene(loader.load(), 900, 600);

        // hand the selected student to the form controller
        DefineStudentsController ctrl = loader.getController();
        ctrl.editExistingStudent(sel);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Edit Student Profile");
        stage.show();
    }

    @FXML
    private void viewComments(javafx.event.ActionEvent event) throws IOException {
        Student sel = studentsTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Select a student to view comments.", ButtonType.OK);
            a.setHeaderText("No selection");
            a.showAndWait();
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/cs151/application/student-comments.fxml"));
        Scene scene = new Scene(loader.load(), 700, 500);

        // Give the selected student to the comments controller
        StudentCommentsController ctrl = loader.getController();
        ctrl.setStudent(sel);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Comments for " + sel.getFullName());
        stage.show();
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

    @FXML
    private void goToSearchStudents(javafx.event.ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/cs151/application/SearchStudents.fxml"));
        Scene scene = new Scene(loader.load());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Search Student Profiles");
        stage.show();
    }
}