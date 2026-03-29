package com.samvit.taskmanager.service;

import com.samvit.taskmanager.dto.TaskCreateDTO;
import com.samvit.taskmanager.dto.TaskResponseDTO;
import com.samvit.taskmanager.exception.TaskNotFoundException;
import com.samvit.taskmanager.model.Task;
import com.samvit.taskmanager.model.User;
import com.samvit.taskmanager.repository.TaskRepository;
import com.samvit.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class) — tells JUnit to use Mockito
// NO Spring context loaded. NO database. NO Tomcat. Pure Java + mocks.
// These tests run in MILLISECONDS.
@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    // @Mock — creates a fake TaskRepository that returns whatever you tell it
    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    // @InjectMocks — creates a REAL TaskServiceImpl and injects the mocks above
    // This works because TaskServiceImpl uses constructor injection
    @InjectMocks
    private TaskServiceImpl taskService;

    // Test data — reused across tests
    private Task sampleTask;
    private User sampleUser;

    // Runs BEFORE each test — creates fresh test data
    @BeforeEach
    void setUp() {
        sampleUser = new User("samvit", "samvit@gmail.com", "hashedpassword");
        sampleUser.setId(1L);
        sampleUser.setCreatedAt(LocalDateTime.now());

        sampleTask = new Task("Learn Spring Boot", "Build a REST API", "pending");
        sampleTask.setId(1L);
        sampleTask.setCreatedAt(LocalDateTime.now());
        sampleTask.setUpdatedAt(LocalDateTime.now());
        sampleTask.setUser(sampleUser);
    }

    // ===== TEST: Get all tasks =====
    @Test
    @DisplayName("getAllTasks should return list of TaskResponseDTOs")
    void getAllTasks_ReturnsListOfDTOs() {
        // ARRANGE — tell the mock what to return
        Task task2 = new Task("Learn JPA", "Relationships", "done");
        task2.setId(2L);
        task2.setCreatedAt(LocalDateTime.now());
        task2.setUpdatedAt(LocalDateTime.now());

        when(taskRepository.findAll()).thenReturn(Arrays.asList(sampleTask, task2));

        // ACT — call the method we're testing
        List<TaskResponseDTO> result = taskService.getAllTasks();

        // ASSERT — verify the result is correct
        assertEquals(2, result.size());
        assertEquals("Learn Spring Boot", result.get(0).getTitle());
        assertEquals("Learn JPA", result.get(1).getTitle());
        assertEquals("samvit", result.get(0).getAssignedTo());

        // VERIFY — the repository method was called exactly once
        verify(taskRepository, times(1)).findAll();
    }

    // ===== TEST: Get task by ID — found =====
    @Test
    @DisplayName("getTaskById should return DTO when task exists")
    void getTaskById_WhenExists_ReturnsDTO() {
        // ARRANGE
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));

        // ACT
        TaskResponseDTO result = taskService.getTaskById(1L);

        // ASSERT
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Learn Spring Boot", result.getTitle());
        assertEquals("pending", result.getStatus());
        assertEquals("samvit", result.getAssignedTo());
    }

    // ===== TEST: Get task by ID — not found (THROWS EXCEPTION) =====
    @Test
    @DisplayName("getTaskById should throw TaskNotFoundException when task doesn't exist")
    void getTaskById_WhenNotExists_ThrowsException() {
        // ARRANGE — return empty Optional (task doesn't exist)
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // ACT & ASSERT — verify the exception is thrown
        TaskNotFoundException exception = assertThrows(
                TaskNotFoundException.class,
                () -> taskService.getTaskById(999L)
        );

        // Verify the exception message contains the ID
        assertTrue(exception.getMessage().contains("999"));
    }

    // ===== TEST: Create task =====
    @Test
    @DisplayName("createTask should save and return DTO")
    void createTask_SavesAndReturnsDTO() {
        // ARRANGE
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setTitle("New Task");
        dto.setDescription("Description");
        dto.setStatus("pending");

        // when save() is called with ANY task, return our sampleTask
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        // ACT
        TaskResponseDTO result = taskService.createTask(dto);

        // ASSERT
        assertNotNull(result);
        assertEquals("Learn Spring Boot", result.getTitle());

        // VERIFY — save was called exactly once
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    // ===== TEST: Create task assigned to user =====
    @Test
    @DisplayName("createTask with userId should assign task to user")
    void createTask_WithUserId_AssignsToUser() {
        // ARRANGE
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setTitle("User's task");
        dto.setUserId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        // ACT
        TaskResponseDTO result = taskService.createTask(dto);

        // ASSERT
        assertNotNull(result);
        assertEquals("samvit", result.getAssignedTo());

        // VERIFY — both repositories were called
        verify(userRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    // ===== TEST: Delete task — exists =====
    @Test
    @DisplayName("deleteTask should call deleteById when task exists")
    void deleteTask_WhenExists_Deletes() {
        // ARRANGE
        when(taskRepository.existsById(1L)).thenReturn(true);

        // ACT
        taskService.deleteTask(1L);

        // VERIFY — deleteById was called
        verify(taskRepository, times(1)).deleteById(1L);
    }

    // ===== TEST: Delete task — not found =====
    @Test
    @DisplayName("deleteTask should throw exception when task doesn't exist")
    void deleteTask_WhenNotExists_ThrowsException() {
        // ARRANGE
        when(taskRepository.existsById(999L)).thenReturn(false);

        // ACT & ASSERT
        assertThrows(
                TaskNotFoundException.class,
                () -> taskService.deleteTask(999L)
        );

        // VERIFY — deleteById was NEVER called (task doesn't exist)
        verify(taskRepository, never()).deleteById(999L);
    }

    // ===== TEST: Get tasks by status =====
    @Test
    @DisplayName("getTasksByStatus should return filtered tasks")
    void getTasksByStatus_ReturnsFilteredList() {
        // ARRANGE
        when(taskRepository.findByStatus("pending"))
                .thenReturn(Arrays.asList(sampleTask));

        // ACT
        List<TaskResponseDTO> result = taskService.getTasksByStatus("pending");

        // ASSERT
        assertEquals(1, result.size());
        assertEquals("pending", result.get(0).getStatus());
    }
}