package com.proj.backend.service;


import com.proj.backend.dto.TaskDto;
import com.proj.backend.model.*;
import com.proj.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ActivityLogService activityLogService;


    // ================================
    // GET TASKS BY GROUP
    // ================================
    public List<TaskDto> getTasksByGroupId(Long groupId) {
        return taskRepository.findByGroupGroupId(groupId)
                .stream()
                .map(TaskDto::fromEntity)
                .toList();
    }

    // ================================\
    // CREATE TASK\
    // ================================\
    // Змінено creatorName -> creatorEmail для надійності
    public TaskDto createTask(Long groupId, String title, String description,
                              String deadlineIso, String creatorEmail) {

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + creatorEmail));

        Task task = Task.builder()
                .group(group)
                .createdBy(creator)
                .title(title)
                .description(description)
                .status(TaskStatus.OPEN)
                .deadline(LocalDateTime.parse(deadlineIso))
                .createdAt(LocalDateTime.now())
                .build();

        Task savedTask = taskRepository.save(task);

        activityLogService.logActivity(
                creator.getUserId(),
                "TASK_CREATED",
                "Created task: " + title + " in group " + group.getName()
        );

        // ✅ ВІДПРАВЛЯЄМО СПОВІЩЕННЯ
        try {
            String msg = "Нове завдання в групі " + group.getName() + ": " + title;
            messagingTemplate.convertAndSend("/topic/notifications", msg);
        } catch (Exception e) {
            System.err.println("WebSocket error: " + e.getMessage());
        }

        return TaskDto.fromEntity(savedTask);
    }

    // ================================
    // GET TASK BY ID
    // ================================
    public TaskDto getTaskById(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        return TaskDto.fromEntity(task);
    }

    // ================================
    // UPDATE TASK (title, desc, deadline)
    // ================================
    public TaskDto updateTask(Long taskId, String newTitle, String newDescription, String newDeadlineIso, String editorEmail) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        User editor = userRepository.findByEmail(editorEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (newTitle != null) task.setTitle(newTitle);
        if (newDescription != null) task.setDescription(newDescription);
        if (newDeadlineIso != null) task.setDeadline(LocalDateTime.parse(newDeadlineIso));

        Task savedTask = taskRepository.save(task);

        // LOG
        activityLogService.logActivity(
                editor.getUserId(),
                "TASK_UPDATED",
                "Updated task: " + savedTask.getTitle()
        );

        return TaskDto.fromEntity(savedTask);
    }

    // ================================
    // UPDATE STATUS
    // ================================
    public TaskDto updateStatus(Long taskId, TaskStatus newStatus, String editorEmail) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        User editor = userRepository.findByEmail(editorEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TaskStatus oldStatus = task.getStatus();
        task.setStatus(newStatus);

        Task savedTask = taskRepository.save(task);

        // LOG
        activityLogService.logActivity(
                editor.getUserId(),
                "TASK_STATUS_CHANGED",
                "Task '" + task.getTitle() + "' changed from " + oldStatus + " to " + newStatus
        );

        return TaskDto.fromEntity(savedTask);
    }

    // ================================
    // DELETE TASK
    // ================================
    public void deleteTask(Long taskId, String editorEmail) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        User editor = userRepository.findByEmail(editorEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String title = task.getTitle();
        String groupName = task.getGroup().getName();

        taskRepository.delete(task);

        // LOG
        activityLogService.logActivity(
                editor.getUserId(),
                "TASK_DELETED",
                "Deleted task: " + title + " from group " + groupName
        );
    }
}