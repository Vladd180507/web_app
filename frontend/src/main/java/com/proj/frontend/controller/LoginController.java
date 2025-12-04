package com.proj.frontend.controller;
import com.proj.frontend.controller.MainMenuController;

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
            openMainMenu(user);

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Login failed");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void openMainMenu(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/main_menu.fxml"));
            Scene scene = new Scene(loader.load(), 1150, 700);

            MainMenuController controller = loader.getController();
            controller.init(user, backendService, stage);

            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleOpenRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/register.fxml"));
            Scene scene = new Scene(loader.load(), 1150, 700);
            scene.getStylesheets().add(
                    App.class.getResource("/css/login.css").toExternalForm()
            );

            RegisterController controller = loader.getController();
            controller.setBackendService(backendService);
            controller.setStage(stage);

            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Navigation error");
            alert.setContentText("Cannot open register screen: " + e.getMessage());
            alert.showAndWait();
        }
    }
}