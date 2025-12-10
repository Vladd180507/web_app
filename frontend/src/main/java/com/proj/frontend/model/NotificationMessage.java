package com.proj.frontend.model;

public class NotificationMessage {
    public String message;
    public Long initiatorId;
    public Long groupId; // ✅ Додано поле для фільтрації

    public NotificationMessage() {
    }

    public NotificationMessage(String message, Long initiatorId, Long groupId) {
        this.message = message;
        this.initiatorId = initiatorId;
        this.groupId = groupId;
    }
}