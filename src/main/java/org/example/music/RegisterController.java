package org.example.music;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label statusLabel;

    @FXML
    private void handleRegister() {
        String u = usernameField.getText().trim();
        String p = passwordField.getText();
        String cp = confirmPasswordField.getText();

        if (u.isEmpty() || p.isEmpty() || cp.isEmpty()) { updateStatus("Заполните все поля!", Color.RED); return; }
        if (u.length() < 3) { updateStatus("Логин минимум 3 символа!", Color.RED); return; }
        if (p.length() < 6) { updateStatus("Пароль минимум 6 символов!", Color.RED); return; }
        if (!p.equals(cp)) { updateStatus("Пароли не совпадают!", Color.RED); return; }

        if (DatabaseHandler.registerUser(u, p)) {
            updateStatus("Регистрация успешна! Входим...", Color.GREEN);
            if (DatabaseHandler.loginUser(u, p)) {
                PauseTransition pause = new PauseTransition(Duration.millis(900));
                pause.setOnFinished(e -> MainApp.showScene("main-view.fxml", "SoundWave - Плеер"));
                pause.play();
            }
        } else {
            updateStatus("Пользователь уже существует!", Color.RED);
        }
    }

    @FXML private void handleBackToLogin() {
        MainApp.showScene("auth-view.fxml", "SoundWave - Вход");
    }

    private void updateStatus(String msg, Color color) {
        if (statusLabel != null) { statusLabel.setText(msg); statusLabel.setTextFill(color); }
    }
}