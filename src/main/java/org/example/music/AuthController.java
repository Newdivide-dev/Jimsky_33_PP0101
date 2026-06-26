package org.example.music;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

public class AuthController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showStatus("Заполните все поля!", Color.RED);
            return;
        }

        if (DatabaseHandler.loginUser(username, password)) {
            showStatus("Вход выполнен успешно!", Color.GREEN);
            MainApp.showScene("main-view.fxml", "SoundWave - Плеер");
        } else {
            showStatus("Неверный логин или пароль!", Color.RED);
        }
    }

    @FXML
    private void handleRegisterNavigation() {
        MainApp.showScene("register-view.fxml", "SoundWave - Регистрация");
    }

    private void showStatus(String message, Color color) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            statusLabel.setTextFill(color);
        }
    }
}