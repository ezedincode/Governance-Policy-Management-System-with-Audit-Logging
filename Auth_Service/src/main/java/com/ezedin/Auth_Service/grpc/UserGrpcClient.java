package com.ezedin.Auth_Service.grpc;

import com.ezedin.Auth_Service.dto.CreateUserRequest;
import com.ezedin.Auth_Service.dto.CreateUserResponse;
import com.ezedin.Auth_Service.exception.DuplicateEmailException;
import com.ezedin.Auth_Service.exception.DuplicateUsernameException;
import com.ezedin.Auth_Service.exception.UserServiceException;
import com.ezedin.grpc.user.*;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class UserGrpcClient {

    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub userStub;

    public UserGrpcResponse findByUsername(String username) {

        UserRequest request = UserRequest.newBuilder()
                .setUsername(username)
                .build();

        return userStub.findByUsername(request);
    }

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
