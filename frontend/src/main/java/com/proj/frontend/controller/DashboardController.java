package com.proj.frontend.controller;

import com.proj.frontend.App;
import com.proj.frontend.model.Group;
import com.proj.frontend.model.User;
import com.proj.frontend.service.BackendService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class DashboardController {

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

        setupTable();
        loadGroups();
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
    }

    private void loadGroups() {
        try {
            List<Group> groups = backendService.getGroups();
            groupsTable.setItems(FXCollections.observableArrayList(groups));
        } catch (Exception e) {
            showError("Failed to load groups: " + e.getMessage());
        }
    }

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
            Scene scene = new Scene(loader.load());

            TasksController controller = loader.getController();
            controller.init(currentUser, group, backendService, stage);

            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Cannot open tasks view: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}