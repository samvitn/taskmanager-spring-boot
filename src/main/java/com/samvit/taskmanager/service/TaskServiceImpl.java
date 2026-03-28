package com.samvit.taskmanager.service;

import com.samvit.taskmanager.dto.TaskCreateDTO;
import com.samvit.taskmanager.dto.TaskResponseDTO;
import com.samvit.taskmanager.exception.TaskNotFoundException;
import com.samvit.taskmanager.model.Task;
import com.samvit.taskmanager.model.User;
import com.samvit.taskmanager.repository.TaskRepository;
import com.samvit.taskmanager.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;   // NEW — needed to look up users

    // Constructor now takes BOTH repositories
    public TaskServiceImpl(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    // UPDATED — now includes assignedTo username
    private TaskResponseDTO toResponseDTO(Task task) {
        String assignedTo = null;
        if (task.getUser() != null) {
            assignedTo = task.getUser().getUsername();
        }
        return new TaskResponseDTO(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus(),
            task.getCreatedAt(),
            task.getUpdatedAt(),
            assignedTo
        );
    }

    private Task toEntity(TaskCreateDTO dto) {
        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        if (dto.getStatus() != null) {
            task.setStatus(dto.getStatus());
        }
        return task;
    }

    @Override
    public List<TaskResponseDTO> getAllTasks() {
        return taskRepository.findAll()
            .stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
    }

    @Override
    public TaskResponseDTO getTaskById(Long id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException(id));
        return toResponseDTO(task);
    }

    @Override
    public TaskResponseDTO createTask(TaskCreateDTO dto) {
        Task task = toEntity(dto);

        // NEW — if userId is provided, assign the task to that user
        if (dto.getUserId() != null) {
            User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getUserId()));
            task.setUser(user);
        }

        Task saved = taskRepository.save(task);
        return toResponseDTO(saved);
    }

    @Override
    public TaskResponseDTO updateTask(Long id, TaskCreateDTO dto) {
        Task existing = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException(id));

        existing.setTitle(dto.getTitle());
        if (dto.getDescription() != null) {
            existing.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null) {
            existing.setStatus(dto.getStatus());
        }
        // NEW — reassign to different user if userId provided
        if (dto.getUserId() != null) {
            User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getUserId()));
            existing.setUser(user);
        }

        Task updated = taskRepository.save(existing);
        return toResponseDTO(updated);
    }

    @Override
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        taskRepository.deleteById(id);
    }

    @Override
    public List<TaskResponseDTO> getTasksByStatus(String status) {
        return taskRepository.findByStatus(status)
            .stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
    }

    @Override
    public List<TaskResponseDTO> searchTasks(String keyword) {
        return taskRepository.findByTitleContainingIgnoreCase(keyword)
            .stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
    }

    @Override
    public long getTasksCount() {
        return taskRepository.count();
    }
}
