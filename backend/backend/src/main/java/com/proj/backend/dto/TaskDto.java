package com.proj.backend.dto;

import com.proj.backend.model.Task;
import com.proj.backend.model.TaskStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TaskDto {
    private Long taskId;
    private Long groupId;
    private String groupName;
    private String createdByName;
    private String title;
    private String description;
    private TaskStatus status;
    private LocalDateTime deadline;
    private LocalDateTime createdAt;

    public static TaskDto fromEntity(Task task) {
        TaskDto dto = new TaskDto();
        dto.setTaskId(task.getTaskId());
        dto.setGroupId(task.getGroup().getGroupId());
        dto.setGroupName(task.getGroup().getName());
        dto.setCreatedByName(task.getCreatedBy().getName());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setDeadline(task.getDeadline());
        dto.setCreatedAt(task.getCreatedAt());
        return dto;
    }
}