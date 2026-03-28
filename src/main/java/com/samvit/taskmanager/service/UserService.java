package com.samvit.taskmanager.service;

import com.samvit.taskmanager.dto.UserCreateDTO;
import com.samvit.taskmanager.dto.UserResponseDTO;

import java.util.List;

public interface UserService {
    List<UserResponseDTO> getAllUsers();
    UserResponseDTO getUserById(Long id);
    UserResponseDTO createUser(UserCreateDTO dto);
    void deleteUser(Long id);
}
