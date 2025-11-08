package App;

import App.Database.MongoDBConnection;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;


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
        java.net.URL cssUrl = getClass().getResource("/styles/app.css");
        System.out.println("DEBUG: cssUrl = " + cssUrl);
        String cssPath = cssUrl == null ? null : cssUrl.toExternalForm();

        primaryStage.setTitle("Digital Business Tracker - Trackify");
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(800);

        try {
            FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent loginRoot = loginLoader.load();

            Scene loginScene = new Scene(loginRoot);

            if (cssPath != null) {
                loginScene.getStylesheets().add(cssPath);
                System.out.println("DEBUG: Added stylesheet to loginScene: " + cssPath);
            } else {
                System.out.println("DEBUG: stylesheet not found, cssPath is null");
            }

            System.out.println("DEBUG: loginScene stylesheets: " + loginScene.getStylesheets());

            loginRoot.setStyle("-fx-background-color: linear-gradient(to bottom, #14171C, #1A1D29);");

            Stage loginStage = new Stage();
            loginStage.initOwner(primaryStage);
            loginStage.initModality(Modality.APPLICATION_MODAL);
            loginStage.setResizable(false);
            loginStage.setTitle("Trackify - Login");
            loginStage.setScene(loginScene);


            loginStage.showAndWait();

            if (!Session.isAuthenticated()) {
                System.out.println("No user logged in. Exiting.");
                MongoDBConnection.close();
                javafx.application.Platform.exit();
                return;
            }

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
