package com.samvit.taskmanager.dto;

import jakarta.validation.constraints.*;

// Updated — now includes optional userId to assign task to a user
public class TaskCreateDTO {

    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 100, message = "Title must be 2-100 characters")
    private String title;

    @Size(max = 500, message = "Description must be under 500 characters")
    private String description;

    @Pattern(regexp = "pending|in_progress|done",
             message = "Status must be: pending, in_progress, or done")
    private String status;

    private Long userId;    // optional — assign task to a user

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
