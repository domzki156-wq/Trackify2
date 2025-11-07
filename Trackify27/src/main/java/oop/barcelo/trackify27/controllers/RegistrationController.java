package oop.barcelo.trackify27.controllers;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import oop.barcelo.trackify27.HelloApplication;
import oop.barcelo.trackify27.db.MongoDBConnection;
import org.bson.Document;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class RegistrationController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField passwordConfirmField;
    @FXML private Label messageLabel;

    @FXML
    private void initialize() {
        messageLabel.setText("");
    }

    @FXML
    private void onRegisterPressed() {
        messageLabel.setText("");
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String pwd = passwordField.getText() == null ? "" : passwordField.getText();
        String pwd2 = passwordConfirmField.getText() == null ? "" : passwordConfirmField.getText();

        if (username.isEmpty() || email.isEmpty() || pwd.isEmpty()) {
            messageLabel.setText("Please fill all fields.");
            return;
        }
        if (!pwd.equals(pwd2)) {
            messageLabel.setText("Passwords do not match.");
            return;
        }

        try {
            MongoDatabase db = MongoDBConnection.getDatabase();
            MongoCollection<Document> users = db.getCollection("users");

            if (users.find(new Document("username", username)).first() != null) {
                messageLabel.setText("Username already taken.");
                return;
            }
            if (users.find(new Document("email", email)).first() != null) {
                messageLabel.setText("Email already registered.");
                return;
            }

            String hash = hashSHA256(pwd);

            Document userDoc = new Document()
                    .append("username", username)
                    .append("email", email)
                    .append("passwordHash", hash)
                    .append("createdAt", System.currentTimeMillis());

            users.insertOne(userDoc);

            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Registered successfully. Redirecting to login...");

            // small delay before switching back (optional) - just open immediately
            Stage stage = (Stage) usernameField.getScene().getWindow();
            HelloApplication.openScene(stage, "/oop/barcelo/trackify27/fxml/login.fxml", "Trackify - Login");

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Failed to register: " + e.getMessage());
        }
    }

    @FXML
    private void onBackToLogin() {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            HelloApplication.openScene(stage, "/oop/barcelo/trackify27/fxml/login.fxml", "Trackify - Login");
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Cannot open login screen: " + e.getMessage());
        }
    }

    private static String hashSHA256(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
