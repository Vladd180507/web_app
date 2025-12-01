package com.proj.frontend.controller;

import com.proj.frontend.App;
import com.proj.frontend.model.Group;
import com.proj.frontend.model.Task;
import com.proj.frontend.model.User;
import com.proj.frontend.service.BackendService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import com.proj.frontend.controller.StatsController;

import java.time.LocalDate;
import java.util.List;

public class TasksController {

    @FXML
    private TableView<Task> tasksTable;

    @FXML
    private TableColumn<Task, Long> idColumn;

    @FXML
    private TableColumn<Task, String> titleColumn;

    @FXML
    private TableColumn<Task, String> descriptionColumn;

    @FXML
    private TableColumn<Task, String> statusColumn;

    @FXML
    private TableColumn<Task, String> deadlineColumn;

    @FXML
    private TextField titleField;

    @FXML
    private TextField descriptionField;

    @FXML
    private DatePicker deadlinePicker;

    @FXML
    private ComboBox<String> statusCombo;

    @FXML
    private Label deadlineSummaryLabel;

    private BackendService backendService;
    private User currentUser;
    private Group currentGroup;
    private Stage stage;

    public void init(User user, Group group, BackendService backendService, Stage stage) {
        this.currentUser = user;
        this.currentGroup = group;
        this.backendService = backendService;
        this.stage = stage;

        stage.setTitle("Tasks - " + group.getName());

        setupTable();
        setupStatusCombo();
        loadTasks();
    }

    @FXML
    private void initialize() {
        setupTable();
        setupStatusCombo();
    }

    private void setupTable() {
        if (idColumn.getCellValueFactory() == null) {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
            deadlineColumn.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        }
    }

    private void setupStatusCombo() {
        if (statusCombo.getItems().isEmpty()) {
            statusCombo.setItems(FXCollections.observableArrayList("OPEN", "IN_PROGRESS", "DONE"));
            statusCombo.getSelectionModel().selectFirst();
        }
    }

    private void loadTasks() {
        try {
            List<Task> tasks = backendService.getTasksByGroup(currentGroup.getId());
            tasksTable.setItems(FXCollections.observableArrayList(tasks));
            updateDeadlineSummary(tasks);
        } catch (Exception e) {
            showError("Failed to load tasks: " + e.getMessage());
        }
    }

    private void updateDeadlineSummary(List<Task> tasks) {
        int overdue = 0;
        int dueToday = 0;

        LocalDate today = LocalDate.now();

        for (Task t : tasks) {
            String dl = t.getDeadline();
            if (dl == null || dl.isBlank()) continue;

            try {
                LocalDate d = LocalDate.parse(dl);
                if (d.isBefore(today)) {
                    overdue++;
                } else if (d.isEqual(today)) {
                    dueToday++;
                }
            } catch (Exception ignored) {
                // неправильний формат – пропускаємо
            }
        }

        if (deadlineSummaryLabel != null) {
            deadlineSummaryLabel.setText(
                    "Deadlines – Overdue: " + overdue + ", Due today: " + dueToday
            );
        }
    }

    @FXML
    private void handleAddTask() {
        String title = titleField.getText();
        String description = descriptionField.getText();
        LocalDate date = deadlinePicker.getValue();

        if (title == null || title.isBlank()) {
            showError("Title is required.");
            return;
        }

        String deadline = (date != null) ? date.toString() : null;

        try {
            Task created = backendService.createTask(currentGroup.getId(), title, description, deadline);
            tasksTable.getItems().add(created);
            updateDeadlineSummary(tasksTable.getItems());

            titleField.clear();
            descriptionField.clear();
            deadlinePicker.setValue(null);

        } catch (Exception e) {
            showError("Failed to create task: " + e.getMessage());
        }
    }

    @FXML
    private void handleChangeStatus() {
        Task selected = tasksTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select a task first.");
            return;
        }

        String newStatus = statusCombo.getValue();
        try {
            Task updated = backendService.updateTaskStatus(selected.getId(), newStatus);
            if (updated != null) {
                selected.setStatus(updated.getStatus());
                tasksTable.refresh();
            }
        } catch (Exception e) {
            showError("Failed to update status: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/dashboard.fxml"));
            Scene scene = new Scene(loader.load());

            DashboardController controller = loader.getController();
            controller.init(currentUser, backendService, stage);

            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            showError("Cannot go back: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    private void handleShowStats() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/stats.fxml"));
            Scene scene = new Scene(loader.load());

            StatsController controller = loader.getController();
            controller.init(currentUser, currentGroup, backendService, stage);

            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Cannot open statistics view: " + e.getMessage());
        }
    }
}