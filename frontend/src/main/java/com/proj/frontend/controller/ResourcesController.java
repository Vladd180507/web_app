package com.proj.frontend.controller;

import com.proj.frontend.App;
import com.proj.frontend.model.Group;
import com.proj.frontend.model.Resource;
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

public class ResourcesController {

    @FXML
    private Label groupLabel;

    @FXML
    private TableView<Resource> resourcesTable;

    @FXML
    private TableColumn<Resource, Long> idColumn;

    @FXML
    private TableColumn<Resource, String> titleColumn;

    @FXML
    private TableColumn<Resource, String> typeColumn;

    @FXML
    private TableColumn<Resource, String> urlColumn;

    @FXML
    private TextField titleField;

    @FXML
    private TextField urlField;

    private BackendService backendService;
    private User currentUser;
    private Group currentGroup;
    private Stage stage;

    public void init(User user, Group group, BackendService backendService, Stage stage) {
        this.currentUser = user;
        this.currentGroup = group;
        this.backendService = backendService;
        this.stage = stage;

        stage.setTitle("Resources - " + group.getName());
        groupLabel.setText("Group: " + group.getName());

        setupTable();
        loadResources();
    }

    @FXML
    private void initialize() {
        setupTable();
    }

    private void setupTable() {
        if (idColumn.getCellValueFactory() == null) {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
            urlColumn.setCellValueFactory(new PropertyValueFactory<>("url"));
        }
    }

    private void loadResources() {
        try {
            List<Resource> resources = backendService.getResourcesByGroup(currentGroup.getId());
            resourcesTable.setItems(FXCollections.observableArrayList(resources));
        } catch (Exception e) {
            showError("Failed to load resources: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddResource() {
        String title = titleField.getText();
        String url = urlField.getText();

        if (title == null || title.isBlank()) {
            showError("Title is required.");
            return;
        }
        if (url == null || url.isBlank()) {
            showError("URL is required.");
            return;
        }

        try {
            Resource created = backendService.createResource(currentGroup.getId(), title, url);
            resourcesTable.getItems().add(created);
            titleField.clear();
            urlField.clear();
        } catch (Exception e) {
            showError("Failed to create resource: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/dashboard.fxml"));
            Scene scene = new Scene(loader.load(), 1150, 700);

            scene.getStylesheets().add(
                    App.class.getResource("/css/dashboard.css").toExternalForm()
            );

            DashboardController controller = loader.getController();
            controller.init(currentUser, backendService, stage);

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