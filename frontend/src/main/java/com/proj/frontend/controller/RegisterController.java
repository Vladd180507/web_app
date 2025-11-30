package com.proj.frontend.controller;

import com.proj.frontend.App;
import com.proj.frontend.model.User;
import com.proj.frontend.service.BackendService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    private BackendService backendService;
    private Stage stage;

    public void setBackendService(BackendService backendService) {
        this.backendService = backendService;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleRegister() {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (name.isBlank() || email.isBlank() || password.isBlank() || confirm.isBlank()) {
            showError("All fields are required.");
            return;
        }

        if (!password.equals(confirm)) {
            showError("Passwords do not match.");
            return;
        }

        try {
            User user = backendService.register(name, email, password);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Registration successful");
            alert.setContentText("User created: " + user.getName());
            alert.showAndWait();

            // після успішної реєстрації повертаємось на екран логіну
            goToLogin();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Registration failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleBackToLogin() {
        goToLogin();
    }

    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/login.fxml"));
            Scene scene = new Scene(loader.load());

            LoginController controller = loader.getController();
            controller.setBackendService(backendService);
            controller.setStage(stage);

            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Cannot open login screen: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}