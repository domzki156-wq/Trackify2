package App.controllers;

import App.dao.DaoFactory;
import App.models.User;
import App.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;
    @FXML private Label messageLabel;

    private final AuthService authService = new AuthService(DaoFactory.getUserDao());

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        char[] password = passwordField.getText().toCharArray();
        char[] confirm = confirmField.getText().toCharArray();

        if (!java.util.Arrays.equals(password, confirm)) {
            messageLabel.setText("Passwords do not match");
            return;
        }
        try {
            User u = authService.register(username, password);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.close();
        } catch (Exception ex) {
            messageLabel.setText("Register failed: " + ex.getMessage());
        } finally {
            java.util.Arrays.fill(password, '\0');
            java.util.Arrays.fill(confirm, '\0');
        }
    }

    @FXML private void handleCancel() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }
}
