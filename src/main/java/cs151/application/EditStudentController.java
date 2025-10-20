package cs151.application;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class EditStudentController {

    @FXML private DialogPane dialogPane;
    @FXML private TextField nameField, jobField, langsField, dbsField;
    @FXML private TextArea  facArea;
    @FXML private ComboBox<String> statusCombo, employedCombo, roleCombo, whiteCombo, blackCombo;

    private Student original;

    @FXML
    public void initialize() {
        statusCombo.getItems().setAll("Freshman", "Sophomore", "Junior", "Senior", "Graduate");
        employedCombo.getItems().setAll("Yes", "No");
        roleCombo.getItems().setAll("Backend Developer", "Frontend Developer", "Full Stack Developer", "Data Engineer");
        whiteCombo.getItems().setAll("Yes", "No");
        blackCombo.getItems().setAll("Yes", "No");
    }

    public void setStudent(Student s) {
        this.original = s;
        if (s == null) return;
        nameField.setText(s.getFullName());
        statusCombo.setValue(emptyToNull(s.getAcademicStatus()));
        employedCombo.setValue(emptyToNull(s.getEmployed()));
        jobField.setText(s.getJobDetails());
        langsField.setText(s.getProgrammingLanguages());
        dbsField.setText(s.getDatabases());
        roleCombo.setValue(emptyToNull(s.getPreferredRole()));
        facArea.setText(s.getFacultyComment());
        whiteCombo.setValue(emptyToNull(s.getWhiteListed()));
        blackCombo.setValue(emptyToNull(s.getBlackListed()));
    }

    private static String emptyToNull(String s) { return (s == null || s.isBlank()) ? null : s; }

    /** Build a new Student from the form (table wants Strings). */
    public Student buildStudent() {
        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        String acad = v(statusCombo), emp = v(employedCombo), job = t(jobField);
        String langs = t(langsField), dbs = t(dbsField), role = v(roleCombo);
        String fac = facArea.getText() == null ? "" : facArea.getText().trim();
        String white = v(whiteCombo), black = v(blackCombo);

        // Student has String-based properties, so construct accordingly
        return new Student(name, acad, emp, job, langs, dbs, role, fac, white, black);
    }

    private static String v(ComboBox<String> cb) { return cb.getValue() == null ? "" : cb.getValue(); }
    private static String t(TextField tf) { return tf.getText() == null ? "" : tf.getText().trim(); }

    public Student getOriginal() { return original; }
}
