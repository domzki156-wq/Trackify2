package App.controllers;

import App.Session;
import App.dao.DaoFactory;
import App.models.User;
import App.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Optional;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private final AuthService authService = new AuthService(DaoFactory.getUserDao());

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        char[] password = passwordField.getText().toCharArray();

        try {
            Optional<User> user = authService.login(username, password);
            if (user.isPresent()) {
                Session.setCurrentUser(user.get());
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.close();
            } else {
                messageLabel.setText("Invalid credentials");
            }
        } catch (Exception ex) {
            messageLabel.setText("Login failed: " + ex.getMessage());
        } finally {
            java.util.Arrays.fill(password, '\0');
        }
    }

    @FXML
    private void openRegister() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            javafx.scene.Parent root = loader.load();
            Stage st = new Stage();
            st.setTitle("Register");
            st.initOwner(usernameField.getScene().getWindow());
            st.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            st.setScene(new javafx.scene.Scene(root));
            st.setResizable(false);
            st.showAndWait();
        } catch (Exception e) {
            messageLabel.setText("Failed to open register: " + e.getMessage());
        }
    }
}
