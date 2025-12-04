package com.proj.frontend.controller;

import com.proj.frontend.App;
import com.proj.frontend.model.User;
import com.proj.frontend.service.BackendService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainMenuController {

    private BackendService backendService;
    private User currentUser;
    private Stage stage;

    public void init(User user, BackendService backendService, Stage stage) {
        this.backendService = backendService;
        this.currentUser = user;
        this.stage = stage;
        stage.setTitle("Main Menu - " + user.getName());
    }

    @FXML
    private void handleGroups() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/dashboard.fxml"));
            Scene scene = new Scene(loader.load());

            DashboardController controller = loader.getController();
            controller.init(currentUser, backendService, stage);

            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleActivity() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/activity_log.fxml"));
            Scene scene = new Scene(loader.load());

            ActivityLogController controller = loader.getController();
            controller.init(currentUser, backendService, stage);

            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/profile.fxml"));
            Scene scene = new Scene(loader.load());

            ProfileController controller = loader.getController();
            controller.init(currentUser, backendService, stage);

            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleStats() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/stats.fxml"));
            Scene scene = new Scene(loader.load());

            StatsController controller = loader.getController();
            controller.init(currentUser, null, backendService, stage);

            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/login.fxml"));
            Scene scene = new Scene(loader.load());

            LoginController controller = loader.getController();
            controller.setBackendService(backendService);
            controller.setStage(stage);

            stage.setScene(scene);
            stage.setTitle("Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAllResources() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/all_resources.fxml"));
            Scene scene = new Scene(loader.load());

            AllResourcesController controller = loader.getController();
            controller.init(currentUser, backendService, stage);

            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}