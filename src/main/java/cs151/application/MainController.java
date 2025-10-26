package cs151.application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.io.IOException;

public class MainController {

    @FXML
    private Label welcomeText;
    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    /**
     * Navigates the application to the Define Programming Languages page.
     * linked via FXML onAction="#goToDefineLanguages".
     */
    @FXML
    protected void goToDefineLanguages() throws IOException {
        //Load the FXML file for the Define Languages page
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/cs151/application/define-languages.fxml"));
        //Create a new Scene using the loaded FXML
        Scene scene = new Scene(fxmlLoader.load(), 900, 600);
        //Get the current window from the welcomeText label
        Stage stage = (Stage) welcomeText.getScene().getWindow();
        //Set the new scene on the current stage and update the window title
        stage.setScene(scene);
        stage.setTitle("Define Programming Languages");
        // Show the stage (optional here since stage is already visible)
        stage.show();
    }

    @FXML
    protected void goToDefineStudents() throws IOException {
        // Load the FXML file for the Define Students page
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/cs151/application/define-students.fxml"));
        // Create a new Scene using the loaded FXML
        Scene scene = new Scene(fxmlLoader.load(), 900, 600);
        // Get the current window (Stage)
        Stage stage = (Stage) welcomeText.getScene().getWindow();
        // Set the new scene and update the title
        stage.setScene(scene);
        stage.setTitle("Define Student Profiles");
        // Show the stage (optional if already visible)
        stage.show();
    }

    @FXML
    protected void goToViewStudents() throws IOException {
        // Load the FXML file for the Define Students page
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/cs151/application/view-students.fxml"));
        // Create a new Scene using the loaded FXML
        Scene scene = new Scene(fxmlLoader.load(), 1280, 600);
        // Get the current window (Stage)
        Stage stage = (Stage) welcomeText.getScene().getWindow();
        // Set the new scene and update the title
        stage.setScene(scene);
        stage.setTitle("All Students Profiles");
        // Show the stage (optional if already visible)
        stage.show();
    }
}