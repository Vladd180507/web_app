package com.proj.frontend.controller;

import com.proj.frontend.App;
import com.proj.frontend.model.ActivityLog;
import com.proj.frontend.model.User;
import com.proj.frontend.service.BackendService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;

public class ActivityLogController {

    @FXML
    private TableView<ActivityLog> logTable;

    @FXML
    private TableColumn<ActivityLog, Long> idColumn;

    @FXML
    private TableColumn<ActivityLog, String> timestampColumn;

    @FXML
    private TableColumn<ActivityLog, Long> userIdColumn;

    @FXML
    private TableColumn<ActivityLog, String> actionColumn;

    @FXML
    private TableColumn<ActivityLog, String> detailsColumn;

    private BackendService backendService;
    private User currentUser;
    private Stage stage;

    public void init(User user, BackendService backendService, Stage stage) {
        this.currentUser = user;
        this.backendService = backendService;
        this.stage = stage;

        stage.setTitle("Activity log");
        setupTable();
        loadLogs();
    }

    @FXML
    private void initialize() {
        setupTable();
    }

    private void setupTable() {
        if (idColumn.getCellValueFactory() == null) {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("logId"));
            timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
            userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
            actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
            detailsColumn.setCellValueFactory(new PropertyValueFactory<>("details"));
        }
    }

    private void loadLogs() {
        try {
            List<ActivityLog> logs = backendService.getActivityLogs();
            logTable.setItems(FXCollections.observableArrayList(logs));
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load activity logs: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/main_menu.fxml"));
            Scene scene = new Scene(loader.load(), 1150, 700);

            scene.getStylesheets().add(
                    App.class.getResource("/css/main_menu.css").toExternalForm()
            );

            MainMenuController controller = loader.getController();
            controller.init(currentUser, backendService, stage); // <-- ось тут

            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Cannot go back to dashboard: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}