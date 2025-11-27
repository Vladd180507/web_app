package com.proj.backend.service;

import com.proj.backend.dto.TaskDto;
import com.proj.backend.model.*;
import com.proj.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    // ================================
    // GET TASKS BY GROUP
    // ================================
    public List<TaskDto> getTasksByGroupId(Long groupId) {
        return taskRepository.findByGroupGroupId(groupId)
                .stream()
                .map(TaskDto::fromEntity)
                .toList();
    }

    // ================================
    // CREATE TASK
    // ================================
    public TaskDto createTask(Long groupId, String title, String description,
                              String deadlineIso, String creatorName) {

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        User creator = userRepository.findByName(creatorName)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Task task = Task.builder()
                .group(group)
                .createdBy(creator)
                .title(title)
                .description(description)
                .deadline(deadlineIso != null ? LocalDateTime.parse(deadlineIso) : null)
                .build();

        return TaskDto.fromEntity(taskRepository.save(task));
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
    public TaskDto updateTask(
            Long taskId,
            String newTitle,
            String newDescription,
            String newDeadlineIso
    ) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (newTitle != null) task.setTitle(newTitle);
        if (newDescription != null) task.setDescription(newDescription);
        if (newDeadlineIso != null) task.setDeadline(LocalDateTime.parse(newDeadlineIso));

        return TaskDto.fromEntity(taskRepository.save(task));
    }

    // ================================
    // UPDATE STATUS
    // ================================
    public TaskDto updateStatus(Long taskId, TaskStatus newStatus) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setStatus(newStatus);
        return TaskDto.fromEntity(taskRepository.save(task));
    }

    // ================================
    // DELETE TASK
    // ================================
    public void deleteTask(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new RuntimeException("Task not found");
        }
        taskRepository.deleteById(taskId);
    }
}
