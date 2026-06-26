package org.example.music;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        showScene("auth-view.fxml", "SoundWave - Вход");
    }

    public static void showScene(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/" + fxml));
            Parent root = loader.load();

            if (primaryStage.getScene() == null) {
                primaryStage.setScene(new Scene(root, 900, 600));
            } else {
                primaryStage.getScene().setRoot(root);
            }

            String css = MainApp.class.getResource("/style.css").toExternalForm();
            primaryStage.getScene().getStylesheets().clear();
            primaryStage.getScene().getStylesheets().add(css);

            primaryStage.setTitle(title);
            primaryStage.setMinWidth(820);
            primaryStage.setMinHeight(520);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Не удалось загрузить сцену: " + fxml);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}