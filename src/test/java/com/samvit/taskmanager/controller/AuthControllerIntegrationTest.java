package com.samvit.taskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samvit.taskmanager.dto.AuthRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthRequestDTO createAuthRequest(String username, String email, String password) {
        AuthRequestDTO req = new AuthRequestDTO();
        req.setUsername(username);
        req.setEmail(email);
        req.setPassword(password);
        return req;
    }

    // ===== TEST: Register — success =====
    @Test
    @DisplayName("POST /api/auth/register should return 201 with token")
    void register_WithValidData_Returns201WithToken() throws Exception {
        AuthRequestDTO request = createAuthRequest("samvit", "samvit@gmail.com", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("samvit@gmail.com"))
                .andExpect(jsonPath("$.message").value("Registration successful"));
    }

    // ===== TEST: Register — duplicate email =====
    @Test
    @DisplayName("POST /api/auth/register with existing email should return 409")
    void register_WithDuplicateEmail_Returns409() throws Exception {
        AuthRequestDTO request = createAuthRequest("samvit", "samvit@gmail.com", "password123");

        // Register first time — success
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Register again with same email — conflict
        AuthRequestDTO duplicate = createAuthRequest("different", "samvit@gmail.com", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already exists: samvit@gmail.com"));
    }

    // ===== TEST: Login — success =====
    @Test
    @DisplayName("POST /api/auth/login with correct credentials should return token")
    void login_WithCorrectCredentials_ReturnsToken() throws Exception {
        // Register first
        AuthRequestDTO request = createAuthRequest("samvit", "samvit@gmail.com", "password123");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Login
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    // ===== TEST: Login — wrong password =====
    @Test
    @DisplayName("POST /api/auth/login with wrong password should return 401")
    void login_WithWrongPassword_Returns401() throws Exception {
        // Register first
        AuthRequestDTO request = createAuthRequest("samvit", "samvit@gmail.com", "password123");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Login with wrong password
        AuthRequestDTO wrongPassword = createAuthRequest("samvit", "samvit@gmail.com", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongPassword)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    // ===== TEST: Register — validation failure =====
    @Test
    @DisplayName("POST /api/auth/register with short password should return 400")
    void register_WithShortPassword_Returns400() throws Exception {
        AuthRequestDTO request = createAuthRequest("samvit", "samvit@gmail.com", "short");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages.password").isNotEmpty());
    }

    // ===== TEST: Auth endpoints are public =====
    @Test
    @DisplayName("Auth endpoints should not require JWT token")
    void authEndpoints_ShouldBePublic() throws Exception {
        AuthRequestDTO request = createAuthRequest("samvit", "samvit@gmail.com", "password123");

        // No Authorization header — should still work
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());  // not 401 or 403
    }
}