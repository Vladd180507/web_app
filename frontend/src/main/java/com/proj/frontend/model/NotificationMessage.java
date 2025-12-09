package com.proj.frontend.model;

public class NotificationMessage {
    // Використовуємо public, щоб Gson міг легко записувати, а ми - читати
    public String message;
    public Long initiatorId;

    public NotificationMessage() {
    }

    public NotificationMessage(String message, Long initiatorId) {
        this.message = message;
        this.initiatorId = initiatorId;
    }
}