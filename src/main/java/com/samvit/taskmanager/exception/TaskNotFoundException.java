package com.samvit.taskmanager.exception;

// Extends RuntimeException = UNCHECKED exception
// Spring won't force callers to try-catch this
public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(Long id) {
        super("Task not found with id: " + id);
    }
}
