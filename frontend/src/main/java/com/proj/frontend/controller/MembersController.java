package com.proj.frontend.controller;

import com.proj.frontend.App;
import com.proj.frontend.model.Group;
import com.proj.frontend.model.Member;
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

public class MembersController {

    @FXML
    private TableView<Member> membersTable;

    @FXML
    private TableColumn<Member, String> nameColumn;

    @FXML
    private TableColumn<Member, String> emailColumn;

    @FXML
    private TableColumn<Member, String> roleColumn;

    @FXML
    private TableColumn<Member, String> joinedAtColumn;

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private ComboBox<String> roleCombo;

    @FXML
    private Label groupNameLabel;

    private BackendService backendService;
    private User currentUser;
    private Group currentGroup;
    private Stage stage;

    public void init(User user, Group group, BackendService backendService, Stage stage) {
        this.currentUser = user;
        this.currentGroup = group;
        this.backendService = backendService;
        this.stage = stage;

        stage.setTitle("Members - " + group.getName());
        groupNameLabel.setText("Members of group: " + group.getName());

        setupTable();
        setupRoleCombo();
        loadMembers();
    }

    @FXML
    private void initialize() {
        setupTable();
        setupRoleCombo();
    }

    private void setupTable() {
        if (nameColumn.getCellValueFactory() == null) {
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
            roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
            joinedAtColumn.setCellValueFactory(new PropertyValueFactory<>("joinedAt"));
        }
    }

    private void setupRoleCombo() {
        if (roleCombo.getItems().isEmpty()) {
            roleCombo.setItems(FXCollections.observableArrayList("ADMIN", "MEMBER"));
            roleCombo.getSelectionModel().select("MEMBER");
        }
    }

    private void loadMembers() {
        try {
            List<Member> list = backendService.getMembersByGroup(currentGroup.getId());
            membersTable.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) {
            showError("Failed to load members: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddMember() {
        String name = nameField.getText();
        String email = emailField.getText();
        String role = roleCombo.getValue();

        if (name == null || name.isBlank() || email == null || email.isBlank()) {
            showError("Name and email are required.");
            return;
        }

        try {
            Member m = backendService.addMemberToGroup(currentGroup.getId(), name, email, role);
            membersTable.getItems().add(m);

            nameField.clear();
            emailField.clear();
            roleCombo.getSelectionModel().select("MEMBER");

        } catch (Exception e) {
            showError("Failed to add member: " + e.getMessage());
        }
    }

    @FXML
    private void handleRemoveMember() {
        Member selected = membersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select a member first.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Remove member");
        confirm.setContentText("Are you sure you want to remove " + selected.getName() + "?");
        var result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean ok = backendService.removeMemberFromGroup(
                        currentGroup.getId(),
                        selected.getUserId()
                );
                if (ok) {
                    membersTable.getItems().remove(selected);
                }
            } catch (Exception e) {
                showError("Failed to remove member: " + e.getMessage());
            }
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
}