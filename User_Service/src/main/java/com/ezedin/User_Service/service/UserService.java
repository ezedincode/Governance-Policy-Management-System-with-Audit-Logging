package com.ezedin.User_Service.service;

import com.ezedin.User_Service.dto.CreateUserRequest;
import com.ezedin.User_Service.dto.UserResponse;
import com.ezedin.User_Service.entity.User;
import com.ezedin.User_Service.exception.DatabaseUnavailableException;
import com.ezedin.User_Service.exception.DuplicateEmailException;
import com.ezedin.User_Service.exception.DuplicateUsernameException;
import com.ezedin.User_Service.exception.UserNotFoundException;
import com.ezedin.User_Service.repository.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Service
@Validated
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @CircuitBreaker(name = "user-database", fallbackMethod = "getUserByIdFallback")
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return toUserResponse(user);
    }

    @CircuitBreaker(name = "user-database", fallbackMethod = "getUserByUsernameFallback")
    public UserResponse getUserByUsername(String username) {
        return toUserResponse(findUserEntityByUsername(username));
    }

    @CircuitBreaker(name = "user-database", fallbackMethod = "findUserEntityByUsernameFallback")
    public User findUserEntityByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
    }

    @Transactional
    @CircuitBreaker(name = "user-database", fallbackMethod = "createUserFallback")
    public UserResponse createUser(@Valid CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUsernameException("Username already exists: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email already exists: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword())
                .role(request.getRole())
                .build();

        User savedUser = userRepository.save(user);
        return toUserResponse(savedUser);
    }

    private UserResponse getUserByIdFallback(UUID id, Throwable t) {
        if (t instanceof UserNotFoundException) {
            throw (UserNotFoundException) t;
        }
        throw new DatabaseUnavailableException("User database is temporarily unavailable", t);
    }

    private UserResponse getUserByUsernameFallback(String username, Throwable t) {
        if (t instanceof UserNotFoundException) {
            throw (UserNotFoundException) t;
        }
        throw new DatabaseUnavailableException("User database is temporarily unavailable", t);
    }

    private User findUserEntityByUsernameFallback(String username, Throwable t) {
        if (t instanceof UserNotFoundException) {
            throw (UserNotFoundException) t;
        }
        throw new DatabaseUnavailableException("User database is temporarily unavailable", t);
    }

    private UserResponse createUserFallback(CreateUserRequest request, Throwable t) {
        if (t instanceof DuplicateUsernameException) {
            throw (DuplicateUsernameException) t;
        }
        if (t instanceof DuplicateEmailException) {
            throw (DuplicateEmailException) t;
        }
        throw new DatabaseUnavailableException("User database is temporarily unavailable", t);
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
