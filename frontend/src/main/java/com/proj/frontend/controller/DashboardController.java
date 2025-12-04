package com.proj.frontend.controller;

import com.proj.frontend.App;
import com.proj.frontend.model.Group;
import com.proj.frontend.model.User;
import com.proj.frontend.service.BackendService;
import com.proj.frontend.service.WebSocketNotificationsClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import com.proj.frontend.controller.MainMenuController;

public class DashboardController {

    private static final boolean ENABLE_WEBSOCKET_NOTIFICATIONS = true;

    // розміри головного вікна застосунку
    private static final double APP_WIDTH = 1150;
    private static final double APP_HEIGHT = 700;

    // CSS для екранів застосунку
    private static final String APP_STYLESHEET = "/css/dashboard.css";
    // CSS для екрану логіну
    private static final String LOGIN_STYLESHEET = "/css/login.css";

    private WebSocketNotificationsClient notificationsClient;

    @FXML
    private TableView<Group> groupsTable;

    @FXML
    private TableColumn<Group, Long> idColumn;

    @FXML
    private TableColumn<Group, String> nameColumn;

    @FXML
    private TableColumn<Group, String> descriptionColumn;

    private BackendService backendService;
    private User currentUser;
    private Stage stage;

    public void init(User user, BackendService backendService, Stage stage) {
        this.currentUser = user;
        this.backendService = backendService;
        this.stage = stage;

        stage.setTitle("Collaborative Study Platform - Dashboard (" + user.getName() + ")");
        stage.setWidth(APP_WIDTH);
        stage.setHeight(APP_HEIGHT);

        setupTable();
        loadGroups();

        if (ENABLE_WEBSOCKET_NOTIFICATIONS) {
            startNotifications();
        }
    }

    @FXML
    private void initialize() {
        setupTable();
    }

    private void setupTable() {
        if (idColumn.getCellValueFactory() == null) {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        }

        // щоб таблиця займала всю ширину красиво
        groupsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // placeholder коли груп немає
        groupsTable.setPlaceholder(new Label("No groups yet. Create your first group!"));
    }

    private void loadGroups() {
        try {
            List<Group> groups = backendService.getGroups();
            groupsTable.setItems(FXCollections.observableArrayList(groups));
        } catch (Exception e) {
            showError("Failed to load groups: " + e.getMessage());
        }
    }

    // --------- helpers для створення сцен ---------

    private Scene createAppScene(FXMLLoader loader) throws IOException {
        Scene scene = new Scene(loader.load(), APP_WIDTH, APP_HEIGHT);
        scene.getStylesheets().add(
                App.class.getResource(APP_STYLESHEET).toExternalForm()
        );
        return scene;
    }

    private Scene createLoginScene(FXMLLoader loader) throws IOException {
        // логін у тебе менший, тому не чіпаю розмір (або постав той, який ти використовуєш)
        Scene scene = new Scene(loader.load(), 1150, 700);
        scene.getStylesheets().add(
                App.class.getResource(LOGIN_STYLESHEET).toExternalForm()
        );
        return scene;
    }

    // ----------------- дії UI -----------------

    @FXML
    private void handleRefresh() {
        loadGroups();
    }

    @FXML
    private void handleAddGroup() {
        TextInputDialog nameDialog = new TextInputDialog();
        nameDialog.setHeaderText("Create new group");
        nameDialog.setContentText("Group name:");
        Optional<String> nameResult = nameDialog.showAndWait();
        if (nameResult.isEmpty() || nameResult.get().isBlank()) {
            return;
        }

        TextInputDialog descDialog = new TextInputDialog();
        descDialog.setHeaderText("Create new group");
        descDialog.setContentText("Description:");
        Optional<String> descResult = descDialog.showAndWait();
        if (descResult.isEmpty()) {
            return;
        }

        try {
            Group created = backendService.createGroup(nameResult.get(), descResult.get());
            groupsTable.getItems().add(created);
        } catch (Exception e) {
            showError("Failed to create group: " + e.getMessage());
        }
    }

    @FXML
    private void handleOpenGroup() {
        Group selected = groupsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a group first.");
            return;
        }
        openTasksForGroup(selected);
    }

    private void openTasksForGroup(Group group) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/tasks.fxml"));
            Scene scene = new Scene(loader.load(), 1150, 700);

            TasksController controller = loader.getController();
            controller.init(currentUser, group, backendService, stage);

            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Cannot open tasks view: " + e.getMessage());
        }
    }

    @FXML
    private void handleOpenResources() {
        Group selected = groupsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a group first.");
            return;
        }
        openResourcesForGroup(selected);
    }

    private void openResourcesForGroup(Group group) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/resources.fxml"));
            Scene scene = new Scene(loader.load(), 1150, 700);

            ResourcesController controller = loader.getController();
            controller.init(currentUser, group, backendService, stage);

            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Cannot open resources view: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    private void handleLogout() {
        if (notificationsClient != null) {
            notificationsClient.disconnect();
        }

        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/login.fxml"));
            Scene scene = new Scene(loader.load(), 1150, 700);

            LoginController controller = loader.getController();
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

    @FXML
    private void handleActivityLog() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/activity_log.fxml"));
            Scene scene = new Scene(loader.load(), 1150, 700);

            ActivityLogController controller = loader.getController();
            controller.init(currentUser, backendService, stage);

            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Cannot open activity log: " + e.getMessage());
        }
    }

    private void startNotifications() {
        notificationsClient = new WebSocketNotificationsClient(message -> {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("Notification");
                alert.setContentText(message);
                alert.show();
            });
        });

        notificationsClient.connect();
    }

    @FXML
    private void handleProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/profile.fxml"));
            Scene scene = new Scene(loader.load(), 1150, 700);

            ProfileController controller = loader.getController();
            controller.init(currentUser, backendService, stage);

            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            showError("Cannot open profile: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditGroup() {
        Group selected = groupsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a group first.");
            return;
        }

        TextInputDialog nameDialog = new TextInputDialog(selected.getName());
        nameDialog.setHeaderText("Edit group");
        nameDialog.setContentText("New name:");
        var nameOpt = nameDialog.showAndWait();
        if (nameOpt.isEmpty() || nameOpt.get().isBlank()) {
            return;
        }

        TextInputDialog descDialog = new TextInputDialog(selected.getDescription());
        descDialog.setHeaderText("Edit group");
        descDialog.setContentText("New description:");
        var descOpt = descDialog.showAndWait();
        if (descOpt.isEmpty()) {
            return;
        }

        try {
            Group updated = backendService.updateGroup(
                    selected.getId(),
                    nameOpt.get(),
                    descOpt.get()
            );

            if (updated != null) {
                selected.setName(updated.getName());
                selected.setDescription(updated.getDescription());
                groupsTable.refresh();
            }

        } catch (Exception e) {
            showError("Failed to update group: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteGroup() {
        Group selected = groupsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a group first.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Delete group");
        confirm.setContentText("Are you sure you want to delete group '" + selected.getName() + "'?");
        var result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean ok = backendService.deleteGroup(selected.getId());
                if (ok) {
                    groupsTable.getItems().remove(selected);
                }
            } catch (Exception e) {
                showError("Failed to delete group: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleOpenMembers() {
        Group selected = groupsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a group first.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/members.fxml"));
            Scene scene = new Scene(loader.load(), 1150, 700);

            MembersController controller = loader.getController();
            controller.init(currentUser, selected, backendService, stage);

            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Cannot open members view: " + e.getMessage());
        }
    }

    @FXML
    private void handleBackToMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/main_menu.fxml"));
            Scene scene = new Scene(loader.load(), 1150, 700);

            scene.getStylesheets().add(
                    App.class.getResource("/css/main_menu.css").toExternalForm()
            );

            MainMenuController controller = loader.getController();
            controller.init(currentUser, backendService, stage);

            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Cannot return to main menu: " + e.getMessage());
        }
    }
}