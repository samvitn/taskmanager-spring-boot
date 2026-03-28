# Task Manager API

A Spring Boot REST API for task management with user assignment, input validation, and global exception handling.

## Tech Stack

- **Java 21** (Temurin)
- **Spring Boot 3.4.4**
- **Spring Data JPA** with Hibernate
- **H2 Database** (in-memory, swappable to PostgreSQL)
- **Maven** for dependency management

## Features

- Full CRUD for Tasks and Users
- User-Task relationships (one user, many tasks)
- Input validation with Bean Validation annotations
- Global exception handling with `@ControllerAdvice`
- DTOs separating API contract from database schema
- Custom query methods (search by status, keyword search)
- Cascade delete (deleting a user removes their tasks)
- Duplicate detection (email and username uniqueness)

## API Endpoints

### Users

| Method | URL | Description | Status |
|--------|-----|-------------|--------|
| GET | `/api/users` | Get all users | 200 |
| GET | `/api/users/{id}` | Get user by ID | 200 / 404 |
| POST | `/api/users` | Create a new user | 201 / 400 / 409 |
| DELETE | `/api/users/{id}` | Delete user and their tasks | 204 / 404 |

### Tasks

| Method | URL | Description | Status |
|--------|-----|-------------|--------|
| GET | `/api/tasks` | Get all tasks | 200 |
| GET | `/api/tasks/{id}` | Get task by ID | 200 / 404 |
| POST | `/api/tasks` | Create a new task | 201 / 400 |
| PUT | `/api/tasks/{id}` | Update a task | 200 / 400 / 404 |
| DELETE | `/api/tasks/{id}` | Delete a task | 204 / 404 |
| GET | `/api/tasks/status?value=done` | Filter tasks by status | 200 |
| GET | `/api/tasks/search?keyword=spring` | Search tasks by title | 200 |
| GET | `/api/tasks/count` | Get total task count | 200 |

## Request Examples

**Create a user:**
```json
POST /api/users
{
    "username": "samvit",
    "email": "samvit@example.com",
    "password": "password123"
}
```

**Create a task assigned to a user:**
```json
POST /api/tasks
{
    "title": "Learn Spring Boot",
    "description": "Build a REST API from scratch",
    "userId": 1
}
```

**Response — task with assigned user:**
```json
{
    "id": 1,
    "title": "Learn Spring Boot",
    "description": "Build a REST API from scratch",
    "status": "pending",
    "createdAt": "2026-03-28T07:02:21.750395",
    "updatedAt": "2026-03-28T07:02:21.750395",
    "assignedTo": "samvit"
}
```

## Architecture

```
Controller Layer    →  Handles HTTP, validation, routing
Service Layer       →  Business logic, DTO ↔ Entity conversion
Repository Layer    →  Database access (Spring Data JPA)
```

Each layer communicates through interfaces, enabling loose coupling and testability.

## Error Handling

All errors return consistent JSON responses:

```json
{
    "timestamp": "2026-03-28T07:05:12.544",
    "status": 404,
    "error": "Not Found",
    "message": "Task not found with id: 999"
}
```

| Status | Scenario |
|--------|----------|
| 400 | Validation failed |
| 404 | Resource not found |
| 409 | Duplicate email or username |
| 500 | Unhandled server error |

## How to Run

1. Clone the repository:
   ```
   git clone https://github.com/samvitn/taskmanager-spring-boot.git
   ```
2. Open in IntelliJ IDEA (or any IDE with Maven support)
3. Wait for Maven to download dependencies
4. Run `TaskManagerApplication.java`
5. API is available at `http://localhost:8080`
6. H2 Console available at `http://localhost:8080/h2-console`

## Project Status

This project is actively being developed. Upcoming features:

- [ ] N+1 query optimization with JOIN FETCH
- [ ] Pagination and sorting
- [ ] Spring Security with JWT authentication
- [ ] Unit and integration tests
- [ ] Spring AI integration
- [ ] PostgreSQL migration

## License

This project is for learning and portfolio purposes.
