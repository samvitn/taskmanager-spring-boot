package com.samvit.taskmanager.security;

import com.samvit.taskmanager.model.User;
import com.samvit.taskmanager.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

// Spring Security needs a way to load users from database.
// This class bridges User entity with Spring Security's UserDetails interface.
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Spring Security calls this method when it needs to authenticate a user.
    // Despite the name "loadUserByUsername", we're using EMAIL as the identifier.
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // Convert User entity into Spring Security's UserDetails format
        // Parameters: username (we use email), password (hashed), authorities (roles/permissions)
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                new ArrayList<>()      // empty authorities for now — we'll add roles later
        );
    }
}