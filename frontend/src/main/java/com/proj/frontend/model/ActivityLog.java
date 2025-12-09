package com.proj.frontend.model;

import com.google.gson.annotations.SerializedName;

public class ActivityLog {
    @SerializedName("logId")
    private Long logId;

    @SerializedName("userId")
    private Long userId;

    @SerializedName("action")
    private String action;

    @SerializedName("details")
    private String details;

    @SerializedName("timestamp")
    private String timestamp; // ApiBackendService автоматично перетворить дату в рядок

    public ActivityLog() {}

    public ActivityLog(Long logId, Long userId, String action, String details, String timestamp) {
        this.logId = logId;
        this.userId = userId;
        this.action = action;
        this.details = details;
        this.timestamp = timestamp;
    }

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}