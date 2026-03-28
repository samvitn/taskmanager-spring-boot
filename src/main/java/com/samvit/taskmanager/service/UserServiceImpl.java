package com.samvit.taskmanager.service;

import com.samvit.taskmanager.dto.UserCreateDTO;
import com.samvit.taskmanager.dto.UserResponseDTO;
import com.samvit.taskmanager.exception.DuplicateResourceException;
import com.samvit.taskmanager.exception.TaskNotFoundException;
import com.samvit.taskmanager.model.User;
import com.samvit.taskmanager.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private UserResponseDTO toResponseDTO(User user) {
        return new UserResponseDTO(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getCreatedAt(),
            user.getTasks().size()   // count of tasks this user has
        );
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
            .stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
    }

    @Override
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return toResponseDTO(user);
    }

    @Override
    public UserResponseDTO createUser(UserCreateDTO dto) {
        // Check for duplicates BEFORE saving
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + dto.getEmail());
        }
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + dto.getUsername());
        }

        User user = new User(dto.getUsername(), dto.getEmail(), dto.getPassword());
        User saved = userRepository.save(user);
        return toResponseDTO(saved);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        // CascadeType.ALL means all user's tasks get deleted too
        userRepository.deleteById(id);
    }
}
