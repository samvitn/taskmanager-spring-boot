package com.samvit.taskmanager.ai;

import com.samvit.taskmanager.dto.TaskCreateDTO;
import com.samvit.taskmanager.dto.TaskResponseDTO;
import com.samvit.taskmanager.model.Task;
import com.samvit.taskmanager.model.User;
import com.samvit.taskmanager.repository.TaskRepository;
import com.samvit.taskmanager.repository.UserRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

// This class contains all the "tools" (functions) that the AI model can call.
// Each @Tool method is like a speed dial button on the AI's phone.
// The AI reads the description to decide WHEN to use each tool.
// The AI reads the parameter descriptions to know WHAT to pass.
//
// CRITICAL SECURITY: Every tool gets the current user from SecurityContext
// so the AI can ONLY access the authenticated user's data.
@Component
public class TaskAiTools {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskAiTools(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    // Helper: get current user from JWT security context
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    // Helper: convert Task entity to a string the AI can understand
    private String taskToString(Task task) {
        return String.format("ID: %d | Title: %s | Status: %s | Description: %s",
                task.getId(),
                task.getTitle(),
                task.getStatus(),
                task.getDescription() != null ? task.getDescription() : "none");
    }

    // ===== TOOL 1: Get all tasks for the current user =====
    // The AI calls this when the user says "show my tasks", "what are my tasks", etc.
    @Tool(description = "Get all tasks belonging to the currently authenticated user. " +
            "Returns a list of tasks with their ID, title, status, and description.")
    public String getUserTasks() {
        User user = getCurrentUser();
        List<Task> tasks = taskRepository.findByUserId(user.getId());

        if (tasks.isEmpty()) {
            return "You have no tasks.";
        }

        return tasks.stream()
                .map(this::taskToString)
                .collect(Collectors.joining("\n"));
    }

    // ===== TOOL 2: Get tasks filtered by status =====
    // The AI calls this when user says "show my pending tasks" or "what's done?"
    @Tool(description = "Get tasks for the current user filtered by status. " +
            "Valid status values are: pending, in_progress, done")
    public String getTasksByStatus(
            @ToolParam(description = "The status to filter by: pending, in_progress, or done")
            String status) {
        User user = getCurrentUser();
        List<Task> tasks = taskRepository.findByUserIdAndStatus(user.getId(), status);

        if (tasks.isEmpty()) {
            return "You have no tasks with status: " + status;
        }

        return tasks.stream()
                .map(this::taskToString)
                .collect(Collectors.joining("\n"));
    }

    // ===== TOOL 3: Create a new task =====
    // The AI calls this when user says "create a task called X" or "add a task to..."
    @Tool(description = "Create a new task for the current user. " +
            "Requires a title. Description and status are optional. " +
            "Default status is 'pending' if not specified.")
    public String createTask(
            @ToolParam(description = "The title for the new task") String title,
            @ToolParam(description = "Optional description for the task") String description,
            @ToolParam(description = "Optional status: pending, in_progress, or done. Defaults to pending.") String status) {
        User user = getCurrentUser();

        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status != null && !status.isBlank() ? status : "pending");
        task.setUser(user);

        Task saved = taskRepository.save(task);
        return "Task created successfully: " + taskToString(saved);
    }

    // ===== TOOL 4: Update task status =====
    // The AI calls this when user says "mark task 3 as done" or "update task status"
    @Tool(description = "Update the status of a specific task. " +
            "The task must belong to the current user. " +
            "Valid status values: pending, in_progress, done")
    public String updateTaskStatus(
            @ToolParam(description = "The ID of the task to update") Long taskId,
            @ToolParam(description = "The new status: pending, in_progress, or done") String newStatus) {
        User user = getCurrentUser();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with ID: " + taskId));

        // SECURITY CHECK: verify the task belongs to the current user
        if (task.getUser() == null || !task.getUser().getId().equals(user.getId())) {
            return "Access denied. This task does not belong to you.";
        }

        task.setStatus(newStatus);
        taskRepository.save(task);
        return "Task " + taskId + " status updated to: " + newStatus;
    }

    // ===== TOOL 5: Delete a task =====
    @Tool(description = "Delete a specific task. The task must belong to the current user.")
    public String deleteTask(
            @ToolParam(description = "The ID of the task to delete") Long taskId) {
        User user = getCurrentUser();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with ID: " + taskId));

        if (task.getUser() == null || !task.getUser().getId().equals(user.getId())) {
            return "Access denied. This task does not belong to you.";
        }

        taskRepository.delete(task);
        return "Task " + taskId + " ('" + task.getTitle() + "') has been deleted.";
    }

    // ===== TOOL 6: Get task statistics =====
    @Tool(description = "Get a summary of the current user's task statistics: " +
            "total count, and count by each status (pending, in_progress, done)")
    public String getTaskStats() {
        User user = getCurrentUser();
        List<Task> tasks = taskRepository.findByUserId(user.getId());

        long total = tasks.size();
        long pending = tasks.stream().filter(t -> "pending".equals(t.getStatus())).count();
        long inProgress = tasks.stream().filter(t -> "in_progress".equals(t.getStatus())).count();
        long done = tasks.stream().filter(t -> "done".equals(t.getStatus())).count();

        return String.format("Task Statistics: Total: %d | Pending: %d | In Progress: %d | Done: %d",
                total, pending, inProgress, done);
    }
}