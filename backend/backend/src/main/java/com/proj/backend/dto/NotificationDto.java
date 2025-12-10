package com.proj.backend.dto;

public record NotificationDto(
        String message,
        Long initiatorId,
        Long groupId // ✅ Обов'язково має бути це поле!
) {}