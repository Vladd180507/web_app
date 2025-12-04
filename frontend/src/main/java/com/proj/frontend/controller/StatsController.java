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
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.List;

public class StatsController {

    @FXML
    private Label groupLabel;

    @FXML
    private PieChart tasksPieChart;

    @FXML
    private Label summaryLabel;

    private BackendService backendService;
    private User currentUser;
    private Group currentGroup;
    private Stage stage;

    public void init(User user, Group group, BackendService backendService, Stage stage) {
        this.currentUser = user;
        this.currentGroup = group;
        this.backendService = backendService;
        this.stage = stage;

        stage.setTitle("Statistics - " + group.getName());
        groupLabel.setText("Group: " + group.getName());

        loadStats();
    }

    @FXML
    private void initialize() {
        // тут поки нічого, бо нам потрібні user/group з init()
    }

    private void loadStats() {
        try {
            List<Task> tasks = backendService.getTasksByGroup(currentGroup.getId());

            long open = tasks.stream().filter(t -> "OPEN".equalsIgnoreCase(t.getStatus())).count();
            long inProgress = tasks.stream().filter(t -> "IN_PROGRESS".equalsIgnoreCase(t.getStatus())).count();
            long done = tasks.stream().filter(t -> "DONE".equalsIgnoreCase(t.getStatus())).count();

            tasksPieChart.setData(FXCollections.observableArrayList(
                    new PieChart.Data("OPEN", open),
                    new PieChart.Data("IN_PROGRESS", inProgress),
                    new PieChart.Data("DONE", done)
            ));

            long total = tasks.size();
            summaryLabel.setText(
                    "Total tasks: " + total +
                            " | OPEN: " + open +
                            " | IN_PROGRESS: " + inProgress +
                            " | DONE: " + done
            );

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load statistics: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/tasks.fxml"));
            Scene scene = new Scene(loader.load());

            TasksController controller = loader.getController();
            controller.init(currentUser, currentGroup, backendService, stage);

            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Cannot go back to tasks: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}