package com.proj.backend.controller;

import com.proj.backend.dto.TaskDto;
import com.proj.backend.model.TaskStatus;
import com.proj.backend.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // ================================
    // GET ALL TASKS BY GROUP
    // ================================
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<TaskDto>> getAllTasks(@PathVariable Long groupId) {
        return ResponseEntity.ok(taskService.getTasksByGroupId(groupId));
    }

    // ================================
    // CREATE TASK IN GROUP
    // ================================
    @PostMapping("/group/{groupId}")
    public ResponseEntity<TaskDto> createTask(
            @PathVariable Long groupId,
            @RequestBody CreateTaskRequest req
    ) {
        TaskDto created = taskService.createTask(
                groupId,
                req.title(),
                req.description(),
                req.deadline(),
                req.creatorName()
        );
        return ResponseEntity.ok(created);
    }

    // ================================
    // GET TASK BY ID
    // ================================
    @GetMapping("/{taskId}")
    public ResponseEntity<TaskDto> getTaskById(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.getTaskById(taskId));
    }

    // ================================
    // UPDATE TASK (title, description, deadline)
    // ================================
    @PutMapping("/{taskId}")
    public ResponseEntity<TaskDto> updateTask(
            @PathVariable Long taskId,
            @RequestBody UpdateTaskRequest req
    ) {
        TaskDto updated = taskService.updateTask(
                taskId,
                req.title(),
                req.description(),
                req.deadline()
        );
        return ResponseEntity.ok(updated);
    }

    // ================================
    // UPDATE STATUS
    // ================================
    @PatchMapping("/{taskId}/status")
    public ResponseEntity<TaskDto> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestBody UpdateStatusRequest req
    ) {
        return ResponseEntity.ok(taskService.updateStatus(taskId, req.status()));
    }

    // ================================
    // DELETE TASK
    // ================================
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    // ================================
    // RECORDS (DTO for controller)
    // ================================
    public record CreateTaskRequest(
            String title,
            String description,
            String deadline,   // ISO format "2025-12-01T20:15:30"
            String creatorName
    ) {}

    public record UpdateTaskRequest(
            String title,
            String description,
            String deadline
    ) {}

    public record UpdateStatusRequest(TaskStatus status) {}
}
