package com.samvit.taskmanager.service;

import com.samvit.taskmanager.dto.AuthRequestDTO;
import com.samvit.taskmanager.dto.AuthResponseDTO;
import com.samvit.taskmanager.exception.DuplicateResourceException;
import com.samvit.taskmanager.model.User;
import com.samvit.taskmanager.repository.UserRepository;
import com.samvit.taskmanager.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    // ===== REGISTER =====
    public AuthResponseDTO register(AuthRequestDTO request) {
        // Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }
        // Check for duplicate username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }

        // Create user with HASHED password — never store plain text
        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword())  // BCrypt hash
        );
        userRepository.save(user);

        // Generate JWT token for the new user
        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponseDTO(token, user.getEmail(), "Registration successful");
    }

    // ===== LOGIN =====
    public AuthResponseDTO login(AuthRequestDTO request) {
        try {
            // AuthenticationManager verifies email + password against the database
            // It uses CustomUserDetailsService to load the user
            // It uses BCryptPasswordEncoder to compare passwords
            // If credentials are wrong, it throws BadCredentialsException
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid email or password");
        }

        // If we reach here, credentials are valid — generate token
        String token = jwtService.generateToken(request.getEmail());

        return new AuthResponseDTO(token, request.getEmail(), "Login successful");
    }
}