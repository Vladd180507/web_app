package com.proj.backend.controller;

import com.proj.backend.dto.TaskDto;
import com.proj.backend.model.TaskStatus;
import com.proj.backend.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    // UserRepository тут більше не потрібен, сервіс сам знайде юзера
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
            @RequestBody CreateTaskRequest req,
            Principal principal // <--- 1. Отримуємо Principal (Spring Security)
    ) {
        // 2. Витягуємо email з токена (JWT Subject = Email)
        String creatorEmail = principal.getName();

        // 3. Передаємо EMAIL у сервіс.
        // Сервіс сам знайде юзера в БД і запише лог.
        TaskDto created = taskService.createTask(
                groupId,
                req.title(),
                req.description(),
                req.deadline(),
                creatorEmail // <--- ВАЖЛИВО: Передаємо email, а не ім'я!
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
    // UPDATE TASK
    @PutMapping("/{taskId}")
    public ResponseEntity<TaskDto> updateTask(
            @PathVariable Long taskId,
            @RequestBody UpdateTaskRequest req,
            java.security.Principal principal // <---
    ) {
        String email = principal.getName();
        TaskDto updated = taskService.updateTask(
                taskId,
                req.title(),
                req.description(),
                req.deadline(),
                email // <--- Передаємо email
        );
        return ResponseEntity.ok(updated);
    }

    // UPDATE STATUS
    @PatchMapping("/{taskId}/status")
    public ResponseEntity<TaskDto> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestBody UpdateStatusRequest req,
            java.security.Principal principal // <---
    ) {
        String email = principal.getName();
        return ResponseEntity.ok(taskService.updateStatus(taskId, req.status(), email));
    }

    // DELETE TASK
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long taskId,
            java.security.Principal principal // <---
    ) {
        String email = principal.getName();
        taskService.deleteTask(taskId, email);
        return ResponseEntity.noContent().build();
    }

    // ================================
    // DTOs
    // ================================
    public record CreateTaskRequest(
            String title,
            String description,
            String deadline,
            String creatorName // Це поле з JSON ігноруємо, беремо з токена
    ) {}

    public record UpdateTaskRequest(
            String title,
            String description,
            String deadline
    ) {}

    public record UpdateStatusRequest(TaskStatus status) {}
}