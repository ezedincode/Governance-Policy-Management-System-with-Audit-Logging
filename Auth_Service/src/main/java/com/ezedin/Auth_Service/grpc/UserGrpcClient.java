package com.ezedin.Auth_Service.grpc;

import com.ezedin.Auth_Service.dto.CreateUserRequest;
import com.ezedin.Auth_Service.dto.CreateUserResponse;
import com.ezedin.Auth_Service.exception.DuplicateEmailException;
import com.ezedin.Auth_Service.exception.DuplicateUsernameException;
import com.ezedin.Auth_Service.exception.UserNotFoundException;
import com.ezedin.Auth_Service.exception.UserServiceException;
import com.ezedin.grpc.user.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class UserGrpcClient {

    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub userStub;

    @CircuitBreaker(name = "user-service", fallbackMethod = "findByUsernameFallback")
    public UserGrpcResponse findByUsername(String username) {
        UserRequest request = UserRequest.newBuilder()
                .setUsername(username)
                .build();

        try {
            return userStub.findByUsername(request);
        } catch (StatusRuntimeException ex) {
            if (ex.getStatus().getCode() == Status.Code.NOT_FOUND) {
                throw new UserNotFoundException("User not found: " + username, ex);
            }
            throw ex;
        }
    }

    @CircuitBreaker(name = "user-service", fallbackMethod = "createUserFallback")
    public CreateUserResponse createUser(CreateUserRequest request) {
        try {
            CreateUserGrpcRequest grpcRequest =
                    CreateUserGrpcRequest.newBuilder()
                            .setUsername(request.getUsername())
                            .setEmail(request.getEmail())
                            .setPassword(request.getPassword())
                            .setRole(Role.valueOf(request.getRole().name()))
                            .build();

            UserGrpcResponse response = userStub.registerUser(grpcRequest);

            return CreateUserResponse.builder()
                    .username(response.getUsername())
                    .email(response.getEmail())
                    .role(com.ezedin.Auth_Service.dto.Role.valueOf(response.getRole()))
                    .build();
        } catch (StatusRuntimeException ex) {
            throw mapGrpcException(ex);
        }
    }

    private UserGrpcResponse findByUsernameFallback(String username, Throwable t) {
        if (t instanceof UserNotFoundException) {
            throw (UserNotFoundException) t;
        }
        throw new UserServiceException("User service is temporarily unavailable", t);
    }

    private CreateUserResponse createUserFallback(CreateUserRequest request, Throwable t) {
        if (t instanceof DuplicateUsernameException) {
            throw (DuplicateUsernameException) t;
        }
        if (t instanceof DuplicateEmailException) {
            throw (DuplicateEmailException) t;
        }
        throw new UserServiceException("User service is temporarily unavailable", t);
    }

    private RuntimeException mapGrpcException(StatusRuntimeException ex) {
        String description = ex.getStatus().getDescription();
        String message = description != null && !description.isBlank()
                ? description
                : "User service request failed";

        if (message.contains("Username already exists")) {
            return new DuplicateUsernameException(message);
        }
        if (message.contains("Email already exists")) {
            return new DuplicateEmailException(message);
        }

        if (ex.getStatus().getCode() == Status.Code.ALREADY_EXISTS) {
            return new DuplicateUsernameException(message);
        }

        return new UserServiceException(message, ex);
    }
}
