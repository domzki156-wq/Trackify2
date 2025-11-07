package oop.barcelo.trackify27.controllers;

import com.mongodb.client.FindIterable;
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

import static com.mongodb.client.model.Filters.or;
import static com.mongodb.client.model.Filters.eq;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    @FXML
    private void initialize() {
        messageLabel.setText("");
    }

    @FXML
    private void onLoginPressed() {
        messageLabel.setText("");
        String userInput = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        if (userInput.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter username/email and password.");
            return;
        }

        try {
            MongoDatabase db = MongoDBConnection.getDatabase();
            MongoCollection<Document> usersColl = db.getCollection("users"); // expects "users"

            FindIterable<Document> found = usersColl.find(or(eq("username", userInput), eq("email", userInput)));
            Document userDoc = found.first();

            System.out.println("DEBUG - userDoc found? " + (userDoc != null));
            if (userDoc != null) {
                System.out.println("DEBUG - userDoc: " + userDoc.toJson());
            }

            if (userDoc == null) {
                messageLabel.setText("No user found with that username/email.");
                return;
            }

            String stored = userDoc.getString("passwordHash");
            if (stored == null) {
                messageLabel.setText("User record has no passwordHash. Please re-register or reset password.");
                return;
            }

            String hashedInput = hashSHA256(password);
            if (stored.equalsIgnoreCase(hashedInput)) {
                // success
                UserController.setCurrentUser(userDoc);
                Stage stage = (Stage) usernameField.getScene().getWindow();
                HelloApplication.openScene(stage, "/oop/barcelo/trackify27/fxml/dashboard.fxml", "Trackify - Dashboard");
            } else {
                messageLabel.setText("Incorrect password. Try again.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            messageLabel.setText("Unable to login: " + ex.getMessage());
        }
    }

    @FXML
    private void onOpenRegistration() {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            HelloApplication.openScene(stage, "/oop/barcelo/trackify27/fxml/registration.fxml", "Trackify - Register");
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Cannot open registration screen: " + e.getMessage());
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
