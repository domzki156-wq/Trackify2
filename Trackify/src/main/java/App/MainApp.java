package App;

import App.Database.MongoDBConnection;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Main entry point for the Trackify application.
 *
 * - Initializes MongoDB connection.
 * - Shows login screen first (styled with app.css).
 * - Opens dashboard after successful authentication.
 * - Closes MongoDB connection on exit.
 */
public class MainApp extends Application {

    @Override
    public void init() throws Exception {
        super.init();

        // Initialize MongoDB connection (uses env var if set)
        String uri = System.getenv("MONGODB_URI");
        String dbName = System.getenv("TRACKIFY_DB"); // optional
        MongoDBConnection.init(uri, dbName);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Preload stylesheet reference and debug
        java.net.URL cssUrl = getClass().getResource("/styles/app.css");
        System.out.println("DEBUG: cssUrl = " + cssUrl);
        String cssPath = cssUrl == null ? null : cssUrl.toExternalForm();

        // Prepare primary stage (owner for dialogs)
        primaryStage.setTitle("Digital Business Tracker - Trackify");
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(800);

        try {
            // 1) Load login UI
            FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent loginRoot = loginLoader.load();

            Scene loginScene = new Scene(loginRoot);

            // Attempt to attach stylesheet if found
            if (cssPath != null) {
                loginScene.getStylesheets().add(cssPath);
                System.out.println("DEBUG: Added stylesheet to loginScene: " + cssPath);
            } else {
                System.out.println("DEBUG: stylesheet not found, cssPath is null");
            }

            // DEBUG: print scene stylesheets
            System.out.println("DEBUG: loginScene stylesheets: " + loginScene.getStylesheets());

            // Fallback visual test: give the login root an inline background so we can see if styling takes
            // (comment out after test)
            loginRoot.setStyle("-fx-background-color: linear-gradient(to bottom, #14171C, #1A1D29);");

            Stage loginStage = new Stage();
            loginStage.initOwner(primaryStage);
            loginStage.initModality(Modality.APPLICATION_MODAL);
            loginStage.setResizable(false);
            loginStage.setTitle("Trackify - Login");
            loginStage.setScene(loginScene);

            // Show and wait for login
            loginStage.showAndWait();

            // If not authenticated, exit
            if (!Session.isAuthenticated()) {
                System.out.println("No user logged in. Exiting.");
                MongoDBConnection.close();
                javafx.application.Platform.exit();
                return;
            }

            // 2) Load dashboard with stylesheet attached
            FXMLLoader dashboardLoader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Parent root = dashboardLoader.load();

            Scene scene = new Scene(root, 1350, 900);
            if (cssPath != null) scene.getStylesheets().add(cssPath);
            System.out.println("DEBUG: dashboard scene stylesheets: " + scene.getStylesheets());

            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Throwable t) {
            t.printStackTrace();
            try { MongoDBConnection.close(); } catch (Exception ex) { ex.printStackTrace(); }
            javafx.application.Platform.exit();
        }
    }


    @Override
    public void stop() throws Exception {
        System.out.println("🧹 Closing MongoDB connection...");
        MongoDBConnection.close();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
