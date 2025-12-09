package com.proj.frontend.controller;

import com.proj.frontend.App;
import com.proj.frontend.model.User;
import com.proj.frontend.service.BackendService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ProfileController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    private User currentUser;
    private BackendService backendService;
    private Stage stage;

    public void init(User user, BackendService backendService, Stage stage) {
        this.currentUser = user;
        this.backendService = backendService;
        this.stage = stage;

        // Заповнюємо поля, якщо користувач переданий
        if (currentUser != null) {
            nameField.setText(currentUser.getName());
            emailField.setText(currentUser.getEmail());
        }
    }

    @FXML
    private void handleSave() {
        String newName = nameField.getText().trim();
        String newEmail = emailField.getText().trim();

        // 1. Валідація: поля не мають бути порожніми
        if (newName.isEmpty() || newEmail.isEmpty()) {
            showError("Name and email cannot be empty.");
            return;
        }

        // 2. Виконуємо запит у фоновому потоці
        new Thread(() -> {
            try {
                // Відправляємо дані на сервер
                User updatedUser = backendService.updateUserProfile(newName, newEmail);

                // 3. Оновлюємо інтерфейс у головному потоці
                Platform.runLater(() -> {
                    // Оновлюємо локальний об'єкт
                    this.currentUser.setName(updatedUser.getName());
                    this.currentUser.setEmail(updatedUser.getEmail());
                    this.currentUser.setId(updatedUser.getId()); // на випадок змін

                    // Показуємо успіх
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Profile updated successfully!");
                    alert.showAndWait();
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("Failed to update profile: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleBack() {
        try {
            // Повернення до головного меню
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main_menu.fxml"));
            Scene scene = new Scene(loader.load(), 1150, 700);

            // Підключаємо стилі (перевір, чи шлях правильний у твоєму проєкті)
            if (getClass().getResource("/css/application.css") != null) {
                scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
            }

            // Передаємо дані назад у контролер меню
            MainMenuController controller = loader.getController();
            controller.init(currentUser, backendService, stage);

            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Cannot return to menu: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}