package com.samvit.taskmanager.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Service
public class AiAssistantService {

    private final ChatClient chatClient;

    public AiAssistantService(ChatClient.Builder builder, TaskAiTools taskAiTools, ChatMemory chatMemory) {
        // ChatMemory is auto-configured by Spring AI — uses InMemoryChatMemoryRepository by default
        this.chatClient = builder
                .defaultSystem("""
                        You are a helpful task management assistant for a productivity application.
                        
                        Your capabilities:
                        - View the user's tasks (use getUserTasks or getTasksByStatus tools)
                        - Create new tasks (use createTask tool)
                        - Update task status (use updateTaskStatus tool)
                        - Delete tasks (use deleteTask tool)
                        - Show task statistics (use getTaskStats tool)
                        
                        Rules:
                        - ALWAYS use the provided tools to fetch or modify tasks. Never make up task data.
                        - When the user asks to see tasks, call the appropriate tool first, then format the response nicely.
                        - When updating or deleting, confirm the action after completion.
                        - If you're unsure which task the user means, ask for clarification.
                        - Be concise but friendly in your responses.
                        - Format task lists clearly with titles, statuses, and IDs.
                        """)
                .defaultTools(taskAiTools)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    public String chat(String userMessage, String conversationId) {
        return this.chatClient.prompt()
                .user(userMessage)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }

    public String generateSummary(String conversationId) {
        String summaryPrompt = """
                Analyze my tasks and give me a productivity summary. Include:
                1. Overview of my task distribution (pending, in progress, done)
                2. A productivity score out of 10
                3. Top 3 actionable suggestions to improve my productivity
                4. Which tasks I should prioritize next
                
                Be specific and reference my actual task titles.
                """;

        return this.chatClient.prompt()
                .user(summaryPrompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }
}