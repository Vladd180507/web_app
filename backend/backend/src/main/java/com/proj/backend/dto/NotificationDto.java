package com.proj.backend.dto;

// Використовуємо record (Java 14+), це ідеально для DTO
public record NotificationDto(
        String message,
        Long initiatorId,
        Long groupId // ✅ Додали ID групи (може бути null)
) {}