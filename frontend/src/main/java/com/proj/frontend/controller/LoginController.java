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

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    private BackendService backendService;
    private Stage stage;

    public void setBackendService(BackendService backendService) {
        this.backendService = backendService;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        try {
            User user = backendService.login(email, password);

            // замість алерта відкриваємо dashboard
            openDashboard(user);

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Login failed");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void openDashboard(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/dashboard.fxml"));
            Scene scene = new Scene(loader.load());

            DashboardController controller = loader.getController();
            controller.init(user, backendService, stage);

            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Navigation error");
            alert.setContentText("Cannot open dashboard: " + e.getMessage());
            alert.showAndWait();
        }
    }
}