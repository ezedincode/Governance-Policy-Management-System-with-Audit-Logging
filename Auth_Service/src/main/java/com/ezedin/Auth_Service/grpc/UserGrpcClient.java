package com.ezedin.Auth_Service.grpc;

import com.ezedin.Auth_Service.dto.CreateUserRequest;
import com.ezedin.Auth_Service.dto.CreateUserResponse;
import com.ezedin.grpc.user.*;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.UUID;

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

        CreateUserGrpcRequest grpcRequest =
                CreateUserGrpcRequest.newBuilder()
                        .setUsername(request.getUsername())
                        .setEmail(request.getEmail())
                        .setPassword(request.getPassword())
                        .setRole(Role.valueOf(request.getRole().name()))
                        .build();

        UserGrpcResponse response =
                userStub.registerUser(grpcRequest);

        UUID UserID = UUID.fromString(response.getId());
        return CreateUserResponse.builder()
                .username(response.getUsername())
                .email(response.getEmail())
                .role(com.ezedin.Auth_Service.dto.Role.valueOf(response.getRole()))
                .build();
    }
}
