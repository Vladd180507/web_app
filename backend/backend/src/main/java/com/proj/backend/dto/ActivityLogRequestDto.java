package com.proj.backend.dto;
import java.time.LocalDateTime;
import com.proj.backend.model.ActivityLog;

// Request DTO (для создания лога, но обычно логи создаются автоматически, так что может не нужен; но для полноты)
public class ActivityLogRequestDto {
    private Long userId;
    private String action;
    private String details;
}

