package com.samvit.taskmanager.controller;

import com.samvit.taskmanager.ai.AiAssistantService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// All AI endpoints are PROTECTED — require JWT token
// The authenticated user's email is used as the conversation ID
// so each user has their own isolated AI conversation and memory
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiAssistantService aiService;

    public AiController(AiAssistantService aiService) {
        this.aiService = aiService;
    }

    // POST /api/ai/chat
    // The main conversational endpoint — user sends a message, AI responds
    // The AI can call tools (read tasks, create tasks, update status, etc.)
    //
    // Example requests a user might send:
    //   "Show me my tasks"
    //   "Create a task called Learn Docker"
    //   "Mark task 3 as done"
    //   "How many pending tasks do I have?"
    //   "Delete task 5"
    //   "What should I work on next?"
    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        String message = request.get("message");
        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message is required"));
        }

        // Use the authenticated user's email as conversation ID
        // This isolates memory per user — User A's conversation doesn't leak into User B's
        String conversationId = authentication.getName();

        String response = aiService.chat(message, conversationId);
        return ResponseEntity.ok(Map.of("response", response));
    }

    // GET /api/ai/summary
    // Generates a productivity summary based on the user's tasks
    // The AI calls getTaskStats and getUserTasks tools automatically
    @GetMapping("/summary")
    public ResponseEntity<Map<String, String>> getSummary(Authentication authentication) {
        String conversationId = authentication.getName() + "-summary";
        String summary = aiService.generateSummary(conversationId);
        return ResponseEntity.ok(Map.of("summary", summary));
    }
}