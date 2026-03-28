package com.samvit.taskmanager.dto;

import java.time.LocalDateTime;

// ===== RESPONSE DTO — what client sees =====
// Notice: NO password field. Never expose passwords in API responses.
public class UserResponseDTO {

    private Long id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
    private int taskCount;    // how many tasks this user has — useful summary

    public UserResponseDTO() {}

    public UserResponseDTO(Long id, String username, String email,
                           LocalDateTime createdAt, int taskCount) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
        this.taskCount = taskCount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getTaskCount() { return taskCount; }
    public void setTaskCount(int taskCount) { this.taskCount = taskCount; }
}
