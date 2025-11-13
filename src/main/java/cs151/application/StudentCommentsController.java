package cs151.application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;

public class StudentCommentsController {

    @FXML private Label studentNameLabel;
    @FXML private ListView<String> commentsListView;
    @FXML private TextArea newCommentArea;

    private Student currentStudent;

    public void setStudent(Student student) {
        this.currentStudent = student;
        studentNameLabel.setText(student.getFullName());
        loadCommentsFromStudent();
    }

    private void loadCommentsFromStudent() {
        commentsListView.getItems().clear();

        if (currentStudent == null) return;

        String allComments = currentStudent.getFacultyComment();
        if (allComments != null && !allComments.isBlank()) {
            // Each comment is stored on its own line
            String[] lines = allComments.split("\\R");
            for (String line : lines) {
                if (!line.isBlank()) {
                    commentsListView.getItems().add(line.trim());
                }
            }
        }
    }

    @FXML
    private void handleAddComment() {
        if (currentStudent == null) return;

        String text = newCommentArea.getText().trim();
        if (text.isEmpty()) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Enter a comment before saving.", ButtonType.OK);
            a.setHeaderText("Empty comment");
            a.showAndWait();
            return;
        }

        // TODO: adjust this format to exactly match item 3 of section 2.1.2
        // Example: [YYYY-MM-DD] Comment text
        String today = LocalDate.now().toString();
        String formatted = "[" + today + "] " + text;

        String existing = currentStudent.getFacultyComment();
        String sep = System.lineSeparator();
        if (existing == null || existing.isBlank()) {
            currentStudent.setFacultyComment(formatted);
        } else {
            currentStudent.setFacultyComment(existing + sep + formatted);
        }

        try {
            // Persist to storage
            StudentStorage.updateStudent(currentStudent.getFullName(), currentStudent);
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to save comment.", ButtonType.OK).showAndWait();
            return;
        }

        newCommentArea.clear();
        loadCommentsFromStudent();
    }

    @FXML
    private void handleBack(javafx.event.ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/cs151/application/view-students.fxml"));
        Scene scene = new Scene(loader.load(), 900, 600);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("View Student Profiles");
        stage.show();
    }
}