package com.ezedin.User_Service.grpc;


import com.ezedin.User_Service.dto.CreateUserRequest;
import com.ezedin.User_Service.dto.UserResponse;
import com.ezedin.User_Service.entity.Role;
import com.ezedin.User_Service.entity.User;
import com.ezedin.User_Service.exception.DuplicateEmailException;
import com.ezedin.User_Service.exception.DuplicateUsernameException;
import com.ezedin.User_Service.repository.UserRepository;
import com.ezedin.User_Service.service.UserService;

import com.ezedin.grpc.user.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.Optional;


@RequiredArgsConstructor
@GrpcService
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {
    private final UserRepository userRepository;
    private final UserService userService;
    @Override
    public void findByUsername(
            UserRequest request,
            StreamObserver<UserGrpcResponse> responseObserver) {

        Optional<User> user = userRepository.findByUsername(request.getUsername());
        if (user.isEmpty()) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("User not found")
                            .asRuntimeException()
            );
            return;
        }
        User foundUser = user.get();
        UserGrpcResponse response = UserGrpcResponse.newBuilder()
                .setId(foundUser.getId().toString())
                .setUsername(foundUser.getUsername())
                .setPassword(foundUser.getPassword())
                .setRole(foundUser.getRole().toString())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void registerUser(
            CreateUserGrpcRequest request,
            StreamObserver<UserGrpcResponse> responseObserver) {

        CreateUserRequest createUserRequest =
                new CreateUserRequest(
                        request.getUsername(),
                        request.getEmail(),
                        request.getPassword(),
                        Role.valueOf(request.getRole().toString())
                );

        UserResponse user = userService.createUser(createUserRequest);

        UserGrpcResponse response =
                UserGrpcResponse.newBuilder()
                        .setId(user.getId().toString())
                        .setUsername(user.getUsername())
                       .setEmail(user.getEmail())
                        .setRole(user.getRole().toString())
                        .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}