package oop.barcelo.trackify27;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Start at the login screen
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/oop/barcelo/trackify27/fxml/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        // apply stylesheet if present
        try {
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/oop/barcelo/trackify27/application.css")).toExternalForm());
        } catch (Exception ignored) {}

        stage.setTitle("Trackify");
        stage.setScene(scene);
        stage.setWidth(1000);
        stage.setHeight(650);
        stage.show();
    }

    public static void openScene(Stage stage, String fxmlPath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(fxmlPath));
        Scene scene = new Scene(loader.load());
        try {
            scene.getStylesheets().add(Objects.requireNonNull(HelloApplication.class.getResource("/oop/barcelo/trackify27/application.css")).toExternalForm());
        } catch (Exception ignored) {}
        stage.setTitle(title);
        stage.setScene(scene);
        stage.sizeToScene();
    }

    public static void main(String[] args) {
        launch();
    }
}
