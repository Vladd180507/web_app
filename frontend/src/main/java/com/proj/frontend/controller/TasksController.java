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
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.control.ButtonBar;
import java.util.Optional;

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

    @FXML
    private Label groupLabel;

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

        if (groupLabel != null) {
            groupLabel.setText("Tasks – " + group.getName());
        }

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
        if (tasks == null || tasks.isEmpty()) {
            deadlineSummaryLabel.setText("No tasks yet.");
            return;
        }

        LocalDate today = LocalDate.now();
        long overdueCount = 0;
        long upcomingCount = 0;

        for (Task t : tasks) {
            // Пропускаємо, якщо дедлайну немає
            if (t.getDeadline() == null || t.getDeadline().isBlank()) {
                continue;
            }

            LocalDate deadline = null;
            try {
                // ✅ ВИПРАВЛЕННЯ: Обробка формату з часом (ISO-8601, наприклад "2023-12-01T14:00:00")
                if (t.getDeadline().contains("T")) {
                    deadline = java.time.LocalDateTime.parse(t.getDeadline()).toLocalDate();
                } else {
                    // Звичайний формат дати ("2023-12-01")
                    deadline = LocalDate.parse(t.getDeadline());
                }
            } catch (Exception e) {
                // Виводимо в консоль помилку, щоб знати, що формат кривий
                System.err.println("Date parse error for task '" + t.getTitle() + "': " + t.getDeadline());
                continue;
            }

            // Перевіряємо статус (null-safe)
            String status = t.getStatus() != null ? t.getStatus() : "";
            boolean isNotDone = !"DONE".equalsIgnoreCase(status);

            if (isNotDone) {
                if (deadline.isBefore(today)) {
                    overdueCount++;
                } else {
                    upcomingCount++;
                }
            }
        }

        deadlineSummaryLabel.setText(
                "Deadlines overdue: " + overdueCount + " | Upcoming: " + upcomingCount
        );
    }

    @FXML
    private void handleAddTask() {
        Dialog<TaskInput> dialog = new Dialog<>();
        dialog.setTitle("New task");
        dialog.setHeaderText("Create new task");

        if (stage != null) {
            dialog.initOwner(stage);
        }

        DialogPane pane = dialog.getDialogPane();
        pane.getStylesheets().add(
                App.class.getResource("/css/task_dialog.css").toExternalForm()
        );
        pane.getStyleClass().add("custom-task-dialog");

        ButtonType createButtonType =
                new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().setAll(createButtonType, ButtonType.CANCEL);

        // === Формуємо вміст ===
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(14);                      // ↑ більший вертикальний відступ
        grid.setPadding(new Insets(20, 20, 20, 20));

        // ЄДИНА СТАНДАРТНА ШИРИНА
        double fieldWidth = 300;

        TextField titleFieldLocal = new TextField();
        titleFieldLocal.setPromptText("Title");
        titleFieldLocal.setPrefWidth(fieldWidth);

        TextField descriptionFieldLocal = new TextField();
        descriptionFieldLocal.setPromptText("Description");
        descriptionFieldLocal.setPrefWidth(fieldWidth);

        DatePicker deadlinePickerLocal = new DatePicker();
        deadlinePickerLocal.setPromptText("Deadline");
        deadlinePickerLocal.setPrefWidth(fieldWidth);

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleFieldLocal, 1, 0);

        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionFieldLocal, 1, 1);

        grid.add(new Label("Deadline:"), 0, 2);
        grid.add(deadlinePickerLocal, 1, 2);

        // → додаємо великий нижній відступ перед кнопками
        GridPane.setMargin(deadlinePickerLocal, new Insets(0, 0, 18, 0));

        pane.setContent(grid);

        Node createButton = pane.lookupButton(createButtonType);
        createButton.setDisable(true);

        titleFieldLocal.textProperty().addListener((obs, oldVal, newVal) ->
                createButton.setDisable(newVal == null || newVal.trim().isEmpty())
        );

        dialog.setResultConverter(button -> {
            if (button == createButtonType) {
                return new TaskInput(
                        titleFieldLocal.getText(),
                        descriptionFieldLocal.getText(),
                        deadlinePickerLocal.getValue()
                );
            }
            return null;
        });

        Optional<TaskInput> result = dialog.showAndWait();
        if (result.isEmpty()) return;

        TaskInput input = result.get();
        String deadline = (input.deadline != null) ? input.deadline.toString() : null;

        try {
            Task created = backendService.createTask(
                    currentGroup.getId(),
                    input.title,
                    input.description,
                    deadline
            );

            tasksTable.getItems().add(created);
            updateDeadlineSummary(tasksTable.getItems());

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
            Scene scene = new Scene(loader.load(), 1150, 700);

            scene.getStylesheets().add(
                    App.class.getResource("/css/dashboard.css").toExternalForm()
            );

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
            Scene scene = new Scene(loader.load(), 1150, 700);

            scene.getStylesheets().add(
                    App.class.getResource("/css/stats.css").toExternalForm()
            );

            StatsController controller = loader.getController();
            controller.init(currentUser, currentGroup, backendService, stage);

            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Cannot open statistics view: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditTask() {
        Task selected = tasksTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select a task first.");
            return;
        }

        // ---------- ДІАЛОГ ДЛЯ РЕДАГУВАННЯ ----------
        Dialog<TaskInput> dialog = new Dialog<>();
        dialog.setTitle("Edit task");
        dialog.setHeaderText("Edit task");

        // стилі – ті ж самі, що й для add task
        dialog.getDialogPane().getStylesheets().add(
                App.class.getResource("/css/task_dialog.css").toExternalForm()
        );
        dialog.getDialogPane().getStyleClass().add("custom-task-dialog");

        ButtonType saveButtonType =
                new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // ---------- ФОРМА ВСЕРЕДИНІ ----------
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(16);
        grid.setPadding(new Insets(20, 25, 30, 25));   // трішки більше знизу, щоб кнопки не були впритул

        TextField titleFieldLocal = new TextField();
        titleFieldLocal.setPromptText("Title");
        titleFieldLocal.setText(selected.getTitle());

        TextField descriptionFieldLocal = new TextField();
        descriptionFieldLocal.setPromptText("Description");
        descriptionFieldLocal.setText(selected.getDescription());

        DatePicker deadlinePickerLocal = new DatePicker();
        deadlinePickerLocal.setPromptText("Deadline");
        if (selected.getDeadline() != null && !selected.getDeadline().isBlank()) {
            try {
                deadlinePickerLocal.setValue(LocalDate.parse(selected.getDeadline()));
            } catch (Exception ignored) {
                // якщо формат не ISO yyyy-MM-dd – просто залишаємо пустим
            }
        }

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleFieldLocal, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionFieldLocal, 1, 1);
        grid.add(new Label("Deadline:"), 0, 2);
        grid.add(deadlinePickerLocal, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Кнопка "Save" активна лише якщо є title
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(titleFieldLocal.getText().trim().isEmpty());

        titleFieldLocal.textProperty().addListener((obs, oldVal, newVal) -> {
            saveButton.setDisable(newVal == null || newVal.trim().isEmpty());
        });

        // конвертація результату в TaskInput
        dialog.setResultConverter(button -> {
            if (button == saveButtonType) {
                return new TaskInput(
                        titleFieldLocal.getText(),
                        descriptionFieldLocal.getText(),
                        deadlinePickerLocal.getValue()
                );
            }
            return null;
        });

        var result = dialog.showAndWait();
        if (result.isEmpty()) {
            return; // Cancel або закрили вікно
        }

        TaskInput input = result.get();
        String newDeadline = (input.deadline != null) ? input.deadline.toString() : null;

        try {
            Task updated = backendService.updateTask(
                    selected.getId(),
                    input.title,
                    input.description,
                    newDeadline
            );

            selected.setTitle(updated.getTitle());
            selected.setDescription(updated.getDescription());
            selected.setDeadline(updated.getDeadline());

            tasksTable.refresh();
            updateDeadlineSummary(tasksTable.getItems());

        } catch (Exception e) {
            showError("Failed to update task: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteTask() {
        Task selected = tasksTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select a task first.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Delete task");
        confirm.setContentText("Are you sure you want to delete this task?");
        var result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean ok = backendService.deleteTask(selected.getId());

                if (ok) {
                    tasksTable.getItems().remove(selected);
                    updateDeadlineSummary(tasksTable.getItems());
                }
            } catch (Exception e) {
                showError("Failed to delete task: " + e.getMessage());
            }
        }
    }

    private static class TaskInput {
        final String title;
        final String description;
        final LocalDate deadline;

        TaskInput(String title, String description, LocalDate deadline) {
            this.title = title;
            this.description = description;
            this.deadline = deadline;
        }
    }
}