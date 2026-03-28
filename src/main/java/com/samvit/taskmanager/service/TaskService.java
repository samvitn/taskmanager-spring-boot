package com.samvit.taskmanager.service;

import com.samvit.taskmanager.dto.TaskCreateDTO;
import com.samvit.taskmanager.dto.TaskResponseDTO;

import java.util.List;

// The CONTRACT — defines what operations are available
// The controller depends on THIS interface, not the implementation
public interface TaskService {

    List<TaskResponseDTO> getAllTasks();

    TaskResponseDTO getTaskById(Long id);

    long getTasksCount();

    TaskResponseDTO createTask(TaskCreateDTO dto);

    TaskResponseDTO updateTask(Long id, TaskCreateDTO dto);

    void deleteTask(Long id);

    List<TaskResponseDTO> getTasksByStatus(String status);

    List<TaskResponseDTO> searchTasks(String keyword);
}
