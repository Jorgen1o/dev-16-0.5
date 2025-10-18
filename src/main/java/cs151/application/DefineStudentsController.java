package cs151.application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;

public class DefineStudentsController {
    @FXML
    private TextField studentNameField;
    @FXML
    private ListView<String> studentListView;

    @FXML
    protected void addStudent() {
        String name = studentNameField.getText();
        if (!name.isEmpty()) {
            studentListView.getItems().add(name);
            studentNameField.clear();
        }
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
