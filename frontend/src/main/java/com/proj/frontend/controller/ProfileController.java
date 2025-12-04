package com.proj.frontend.controller;

import com.proj.frontend.App;
import com.proj.frontend.model.User;
import com.proj.frontend.service.BackendService;
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

        // Заповнити поля поточними даними
        nameField.setText(user.getName());
        emailField.setText(user.getEmail());
    }

    @FXML
    private void handleSave() {
        try {
            String newName = nameField.getText();
            String newEmail = emailField.getText();

            if (newName.isBlank() || newEmail.isBlank()) {
                showError("Name and email cannot be empty.");
                return;
            }

            User updated = backendService.updateUserProfile(newName, newEmail);
            currentUser.setName(updated.getName());
            currentUser.setEmail(updated.getEmail());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Success");
            alert.setContentText("Profile updated successfully!");
            alert.showAndWait();

        } catch (Exception e) {
            showError("Failed to update profile: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/dashboard.fxml"));
            Scene scene = new Scene(loader.load());

            DashboardController controller = loader.getController();
            controller.init(currentUser, backendService, stage);

            stage.setTitle("Collaborative Study Platform - Dashboard");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            showError("Cannot return: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}