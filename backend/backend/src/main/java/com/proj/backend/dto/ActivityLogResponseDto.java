package com.proj.backend.dto;

import com.proj.backend.model.ActivityLog;

import java.time.LocalDateTime;

// Response DTO
import lombok.Data;

@Data
public class ActivityLogResponseDto {
    private Long logId;
    private String action;
    private String details;
    private LocalDateTime timestamp;
    private Long userId;

    // Конструктор для маппинга из ActivityLog
    public ActivityLogResponseDto(ActivityLog log) {
        this.logId = log.getLogId();
        this.action = log.getAction();
        this.details = log.getDetails();
        this.timestamp = log.getTimestamp();
        this.userId = log.getUser().getUserId();
    }
}
