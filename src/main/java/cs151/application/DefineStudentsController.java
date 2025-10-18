package cs151.application;

import javafx.scene.control.*;
import java.io.IOException;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;

public class DefineStudentsController {
    @FXML
    private TextField studentNameField;
    @FXML
    private ListView<String> studentListView;

    @FXML
    private ComboBox<String> academicStatusCombo;
    @FXML
    private RadioButton employedRadio;
    @FXML
    private RadioButton notEmployedRadio;
    @FXML
    private TextField jobDetailsField;
    @FXML
    private ListView<String> languagesList;
    @FXML
    private ListView<String> databaseList;
    @FXML
    private ComboBox<String> professionalRoleCombo;


    @FXML
    protected void addStudent() throws IOException {
        String name = studentNameField.getText().trim();
        if (!name.isEmpty()) {
            studentListView.getItems().add(name);
            studentNameField.clear();
        }
        String academicStatus = academicStatusCombo.getValue();
        boolean employed = employedRadio.isSelected();
        String jobDetails;
        if( employed ) {
            jobDetails = jobDetailsField.getText().trim();
        }else{
            jobDetails ="";
        }
        List<String> languages = languagesList.getSelectionModel().getSelectedItems();
        List<String> database = databaseList.getSelectionModel().getSelectedItems();
        String preferredRole = professionalRoleCombo.getValue();

        String output = "\"" + name + "\"," +  "\"" + academicStatus + "\"," + "\"" + (employed ? "Yes" : "No") + "\"," + "\"" + jobDetails + "\"," + "\"" + String.join(";", languages) + "\"," +  "\"" + String.join(";", database) + "\"," +  "\"" + preferredRole + "\"";


    }

    @FXML
    protected void goBack(javafx.event.ActionEvent event) throws IOException {
        //Loads FXML for home screen
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/cs151/application/hello-view.fxml"));
        //Creates new scene with loaded FXML
        Scene scene = new Scene(fxmlLoader.load(), 800, 500);
        //Gets currents stage from event source
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        //Sets new scene
        stage.setScene(scene);
        stage.setTitle("Home");
        stage.show();
    }
}
