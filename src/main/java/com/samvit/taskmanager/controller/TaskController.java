package com.samvit.taskmanager.controller;

import com.samvit.taskmanager.dto.TaskCreateDTO;
import com.samvit.taskmanager.dto.TaskResponseDTO;
import com.samvit.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController                    // This is a REST API controller (returns JSON)
@RequestMapping("/api/tasks")      // Base URL: all endpoints start with /api/tasks
public class TaskController {

    private final TaskService taskService;

    // Constructor injection — depends on TaskService INTERFACE, not implementation
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // GET /api/tasks
    // Returns all tasks
    @GetMapping
    public List<TaskResponseDTO> getAllTasks() {
        return taskService.getAllTasks();
    }

    // GET /api/tasks/5
    // Returns one task by ID
    @GetMapping("/{id}")
    public TaskResponseDTO getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id);
    }

    // POST /api/tasks
    // Creates a new task
    // @Valid triggers validation on TaskCreateDTO before this method runs
    @PostMapping
    public ResponseEntity<TaskResponseDTO> createTask(@Valid @RequestBody TaskCreateDTO dto) {
        TaskResponseDTO created = taskService.createTask(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);  // 201 Created
    }

    // PUT /api/tasks/5
    // Updates an existing task
    @PutMapping("/{id}")
    public TaskResponseDTO updateTask(@PathVariable Long id,
                                      @Valid @RequestBody TaskCreateDTO dto) {
        return taskService.updateTask(id, dto);
    }

    // DELETE /api/tasks/5
    // Deletes a task
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();  // 204 No Content
    }

    // GET /api/tasks/status?value=done
    // Filter tasks by status
    @GetMapping("/status")
    public List<TaskResponseDTO> getTasksByStatus(@RequestParam String value) {
        return taskService.getTasksByStatus(value);
    }

    // GET /api/tasks/search?keyword=spring
    // Search tasks by title
    @GetMapping("/search")
    public List<TaskResponseDTO> searchTasks(@RequestParam String keyword) {
        return taskService.searchTasks(keyword);
    }
}
