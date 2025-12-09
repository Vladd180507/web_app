package com.proj.frontend.controller;

import com.proj.frontend.App;
import com.proj.frontend.model.ActivityLog;
import com.proj.frontend.model.User;
import com.proj.frontend.service.BackendService;
import javafx.application.Platform;
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

    @FXML private TableView<ActivityLog> logTable;
    @FXML private TableColumn<ActivityLog, Long> idColumn;
    @FXML private TableColumn<ActivityLog, String> timestampColumn;
    @FXML private TableColumn<ActivityLog, Long> userIdColumn;
    @FXML private TableColumn<ActivityLog, String> actionColumn;
    @FXML private TableColumn<ActivityLog, String> detailsColumn;

    private BackendService backendService;
    private User currentUser;
    private Stage stage;

    // Цей метод викликається автоматично при завантаженні FXML
    @FXML
    public void initialize() {
        // Прив'язуємо поля класу ActivityLog до колонок таблиці
        idColumn.setCellValueFactory(new PropertyValueFactory<>("logId"));
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
        detailsColumn.setCellValueFactory(new PropertyValueFactory<>("details"));
    }

    public void init(User user, BackendService backendService, Stage stage) {
        this.currentUser = user;
        this.backendService = backendService;
        this.stage = stage;

        loadLogs();
    }

    private void loadLogs() {
        if (backendService == null) return;

        // Завантажуємо дані у фоновому потоці
        new Thread(() -> {
            try {
                List<ActivityLog> logs = backendService.getActivityLogs();

                // Оновлюємо UI в головному потоці JavaFX
                Platform.runLater(() -> {
                    logTable.setItems(FXCollections.observableArrayList(logs));
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("Failed to load activity logs: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleBack() {
        try {
            // Повертаємося до головного меню (або Dashboard)
            // Переконайся, що шлях /fxml/main_menu.fxml правильний
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main_menu.fxml"));
            Scene scene = new Scene(loader.load(), 1150, 700);

            // Якщо є CSS, додаємо його
            String css = getClass().getResource("/css/application.css") != null
                    ? getClass().getResource("/css/application.css").toExternalForm()
                    : null;
            if (css != null) scene.getStylesheets().add(css);

            // Ініціалізуємо контролер меню
            MainMenuController controller = loader.getController();
            controller.init(currentUser, backendService, stage);

            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            // Якщо main_menu немає, спробуй завантажити dashboard.fxml
            showError("Cannot go back: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadLogs();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}