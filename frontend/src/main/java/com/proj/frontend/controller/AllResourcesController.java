package com.proj.frontend.controller;

import com.proj.frontend.App;
import com.proj.frontend.model.Resource;
import com.proj.frontend.model.User;
import com.proj.frontend.service.BackendService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;

public class AllResourcesController {

    @FXML
    private TableView<Resource> resourcesTable;

    @FXML
    private TableColumn<Resource, Long> groupColumn;

    @FXML
    private TableColumn<Resource, String> titleColumn;

    @FXML
    private TableColumn<Resource, String> urlColumn;

    private BackendService backendService;
    private User currentUser;
    private Stage stage;

    public void init(User user, BackendService backendService, Stage stage) {
        this.currentUser = user;
        this.backendService = backendService;
        this.stage = stage;

        stage.setTitle("All Resources");

        setupTable();
        loadResources();
    }

    private void setupTable() {
        groupColumn.setCellValueFactory(new PropertyValueFactory<>("groupId"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        urlColumn.setCellValueFactory(new PropertyValueFactory<>("url"));
    }

    private void loadResources() {
        try {
            List<Resource> list = backendService.getAllResources();
            resourcesTable.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/main_menu.fxml"));
            Scene scene = new Scene(loader.load());

            MainMenuController controller = loader.getController();
            controller.init(currentUser, backendService, stage);

            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}