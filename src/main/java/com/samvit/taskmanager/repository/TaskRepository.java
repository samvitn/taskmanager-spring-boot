package com.samvit.taskmanager.repository;

import com.samvit.taskmanager.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// JpaRepository<Task, Long> means:
//   Task = the entity type this repository manages
//   Long = the type of Task's primary key (id field)
//
// By extending JpaRepository, you get these methods FOR FREE:
//   findAll()         → List<Task>         — SELECT * FROM tasks
//   findById(Long id) → Optional<Task>     — SELECT * FROM tasks WHERE id = ?
//   save(Task task)   → Task               — INSERT or UPDATE
//   deleteById(Long id) → void             — DELETE FROM tasks WHERE id = ?
//   count()           → long               — SELECT COUNT(*) FROM tasks
//   existsById(Long id) → boolean          — does this id exist?
//
// You write ZERO implementation code. Spring generates it all.

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Custom query methods — Spring reads the method name and generates SQL!
    // findByStatus("done") → SELECT * FROM tasks WHERE status = 'done'
    List<Task> findByStatus(String status);

    // findByTitleContainingIgnoreCase("spring")
    // → SELECT * FROM tasks WHERE LOWER(title) LIKE '%spring%'
    List<Task> findByTitleContainingIgnoreCase(String keyword);
}
