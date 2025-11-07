package oop.barcelo.trackify27.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import oop.barcelo.trackify27.HelloApplication;
import org.bson.Document;

public class DashboardController {

    @FXML private Label welcomeLabel;

    @FXML
    private void initialize() {
        Document user = UserController.getCurrentUser();
        if (user != null) {
            String name = user.getString("username");
            if (name == null || name.isBlank()) name = user.getString("email");
            welcomeLabel.setText("Welcome, " + (name == null ? "User" : name) + "!");
        } else {
            welcomeLabel.setText("Welcome!");
        }
    }

    @FXML
    private void onLogout() {
        UserController.clear();
        try {
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            HelloApplication.openScene(stage, "/oop/barcelo/trackify27/fxml/login.fxml", "Trackify - Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
