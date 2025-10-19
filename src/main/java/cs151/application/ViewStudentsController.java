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
    protected void goBack(javafx.event.ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/cs151/application/hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 500);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Home");
        stage.show();
    }
}