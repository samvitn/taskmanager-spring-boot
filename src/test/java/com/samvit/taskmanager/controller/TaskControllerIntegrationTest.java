package com.samvit.taskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samvit.taskmanager.dto.AuthRequestDTO;
import com.samvit.taskmanager.dto.AuthResponseDTO;
import com.samvit.taskmanager.dto.TaskCreateDTO;
import com.samvit.taskmanager.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @SpringBootTest — loads the FULL application context (all beans, real database)
// @AutoConfigureMockMvc — creates MockMvc to send fake HTTP requests
// @DirtiesContext — resets the database after each test class (clean state)
// These tests are SLOWER than unit tests but test the full pipeline.
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;  // sends fake HTTP requests without starting Tomcat

    @Autowired
    private ObjectMapper objectMapper;  // converts Java objects to JSON strings

    @Autowired
    private AuthService authService;

    private String jwtToken;

    // Before each test: register a user and get a JWT token
    @BeforeEach
    void setUp() {
        AuthRequestDTO authRequest = new AuthRequestDTO();
        authRequest.setUsername("testuser");
        authRequest.setEmail("test@example.com");
        authRequest.setPassword("password123");

        AuthResponseDTO authResponse = authService.register(authRequest);
        jwtToken = authResponse.getToken();
    }

    // ===== TEST: Create task — success =====
    @Test
    @DisplayName("POST /api/tasks should create task and return 201")
    void createTask_WithValidData_Returns201() throws Exception {
        TaskCreateDTO task = new TaskCreateDTO();
        task.setTitle("Integration Test Task");
        task.setDescription("Testing the full pipeline");

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isCreated())                          // 201
                .andExpect(jsonPath("$.title").value("Integration Test Task"))
                .andExpect(jsonPath("$.status").value("pending"))         // default status
                .andExpect(jsonPath("$.id").isNumber())                   // auto-generated
                .andExpect(jsonPath("$.createdAt").isNotEmpty());         // auto-set
    }

    // ===== TEST: Create task — validation failure =====
    @Test
    @DisplayName("POST /api/tasks with empty title should return 400")
    void createTask_WithEmptyTitle_Returns400() throws Exception {
        TaskCreateDTO task = new TaskCreateDTO();
        task.setTitle("");  // invalid — @NotBlank

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isBadRequest())                      // 400
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.messages.title").isNotEmpty());    // field-specific error
    }

    // ===== TEST: Get task — not found =====
    @Test
    @DisplayName("GET /api/tasks/999 should return 404")
    void getTaskById_WhenNotExists_Returns404() throws Exception {
        mockMvc.perform(get("/api/tasks/999")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound())                        // 404
                .andExpect(jsonPath("$.message").value("Task not found with id: 999"));
    }

    // ===== TEST: Get all tasks — empty list =====
    @Test
    @DisplayName("GET /api/tasks should return empty list initially")
    void getAllTasks_WhenEmpty_ReturnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())                              // 200
                .andExpect(jsonPath("$", hasSize(0)));                   // empty array
    }

    // ===== TEST: Create then get task =====
    @Test
    @DisplayName("POST then GET should return the created task")
    void createThenGet_ReturnsCreatedTask() throws Exception {
        // Create a task
        TaskCreateDTO task = new TaskCreateDTO();
        task.setTitle("Roundtrip Test");

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isCreated());

        // Get all tasks — should contain the one we just created
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Roundtrip Test"));
    }

    // ===== TEST: Delete task =====
    @Test
    @DisplayName("DELETE /api/tasks/{id} should return 204")
    void deleteTask_WhenExists_Returns204() throws Exception {
        // First create a task
        TaskCreateDTO task = new TaskCreateDTO();
        task.setTitle("To be deleted");

        String response = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andReturn().getResponse().getContentAsString();

        Long taskId = objectMapper.readTree(response).get("id").asLong();

        // Delete it
        mockMvc.perform(delete("/api/tasks/" + taskId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());                      // 204

        // Verify it's gone
        mockMvc.perform(get("/api/tasks/" + taskId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());                       // 404
    }

    // ===== TEST: Accessing without token — should be forbidden =====
    @Test
    @DisplayName("GET /api/tasks without token should return 403")
    void getTasksWithoutToken_Returns403() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isForbidden());                      // 403
    }

    // ===== TEST: Invalid status validation =====
    @Test
    @DisplayName("POST /api/tasks with invalid status should return 400")
    void createTask_WithInvalidStatus_Returns400() throws Exception {
        TaskCreateDTO task = new TaskCreateDTO();
        task.setTitle("Valid title");
        task.setStatus("banana");  // invalid — not in pending|in_progress|done

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages.status").isNotEmpty());
    }
}