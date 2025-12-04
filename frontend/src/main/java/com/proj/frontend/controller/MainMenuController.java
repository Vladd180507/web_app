package com.proj.frontend.controller;

import com.proj.frontend.App;
import com.proj.frontend.model.User;
import com.proj.frontend.service.BackendService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class MainMenuController {

    private static final double APP_WIDTH = 1150;
    private static final double APP_HEIGHT = 700;

    private static final String LOGIN_CSS      = "/css/login.css";
    private static final String DASHBOARD_CSS  = "/css/dashboard.css";
    private static final String PROFILE_CSS    = "/css/profile.css";
    private static final String MAIN_MENU_CSS  = "/css/main_menu.css";

    private BackendService backendService;
    private User currentUser;
    private Stage stage;

    public void init(User user, BackendService backendService, Stage stage) {
        this.currentUser = user;
        this.backendService = backendService;
        this.stage = stage;

        stage.setTitle("Main Menu - " + user.getName());
        stage.setWidth(APP_WIDTH);
        stage.setHeight(APP_HEIGHT);
    }

    /* ---------- helpers ---------- */

    private Scene loadScene(String fxmlPath, String cssPath, double width, double height) throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource(fxmlPath));
        Scene scene = new Scene(loader.load(), width, height);

        if (cssPath != null) {
            scene.getStylesheets().add(
                    App.class.getResource(cssPath).toExternalForm()
            );
        }

        // повертаємо сцену та даємо можливість викликати loader.getController() зовні
        this.lastLoader = loader;
        return scene;
    }

    // щоб після loadScene мати доступ до контролера
    private FXMLLoader lastLoader;

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /* ---------- button handlers (під onAction у FXML) ---------- */

    @FXML
    private void handleOpenGroups() {
        try {
            Scene scene = loadScene("/fxml/dashboard.fxml", DASHBOARD_CSS, APP_WIDTH, APP_HEIGHT);

            DashboardController controller = lastLoader.getController();
            controller.init(currentUser, backendService, stage);

            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Cannot open dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void handleOpenActivityLog() {
        try {
            Scene scene = loadScene("/fxml/activity_log.fxml", DASHBOARD_CSS, APP_WIDTH, APP_HEIGHT);

            ActivityLogController controller = lastLoader.getController();
            controller.init(currentUser, backendService, stage);

            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Cannot open activity log: " + e.getMessage());
        }
    }

    @FXML
    private void handleOpenProfile() {
        try {
            Scene scene = loadScene("/fxml/profile.fxml", PROFILE_CSS, 550, 750);

            ProfileController controller = lastLoader.getController();
            controller.init(currentUser, backendService, stage);

            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Cannot open profile: " + e.getMessage());
        }
    }

    @FXML
    private void handleOpenAllResources() {

        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/activity_log.fxml"));
            Scene scene = new Scene(loader.load(), 1150, 700);

            scene.getStylesheets().add(
                    App.class.getResource("/css/activity_log.css").toExternalForm()
            );

            ActivityLogController controller = loader.getController();
            controller.init(currentUser, backendService, stage);

            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Cannot open resources: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        try {
            Scene scene = loadScene("/fxml/login.fxml", LOGIN_CSS, 550, 750);

            LoginController controller = lastLoader.getController();
            controller.setBackendService(backendService);
            controller.setStage(stage);

            stage.setTitle("Collaborative Study Platform - Login");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Cannot logout: " + e.getMessage());
        }
    }
}