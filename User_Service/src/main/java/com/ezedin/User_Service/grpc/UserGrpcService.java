package com.ezedin.User_Service.grpc;


import com.ezedin.User_Service.dto.CreateUserRequest;
import com.ezedin.User_Service.dto.UserResponse;
import com.ezedin.User_Service.entity.User;
import com.ezedin.User_Service.exception.DatabaseUnavailableException;
import com.ezedin.User_Service.exception.UserNotFoundException;
import com.ezedin.User_Service.service.UserService;

import com.ezedin.grpc.user.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;


@RequiredArgsConstructor
@GrpcService
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

    private final UserService userService;

    @Override
    public void findByUsername(
            UserRequest request,
            StreamObserver<UserGrpcResponse> responseObserver) {

        try {
            User foundUser = userService.findUserEntityByUsername(request.getUsername());
            UserGrpcResponse response = UserGrpcResponse.newBuilder()
                    .setId(foundUser.getId().toString())
                    .setUsername(foundUser.getUsername())
                    .setPassword(foundUser.getPassword())
                    .setRole(foundUser.getRole().toString())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (UserNotFoundException ex) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription(ex.getMessage())
                            .asRuntimeException()
            );
        } catch (DatabaseUnavailableException ex) {
            responseObserver.onError(
                    Status.UNAVAILABLE
                            .withDescription(ex.getMessage())
                            .asRuntimeException()
            );
        }
    }

    @Override
    public void registerUser(
            CreateUserGrpcRequest request,
            StreamObserver<UserGrpcResponse> responseObserver) {

        try {
            CreateUserRequest createUserRequest =
                    new com.ezedin.User_Service.dto.CreateUserRequest(
                            request.getUsername(),
                            request.getEmail(),
                            request.getPassword(),
                            com.ezedin.User_Service.entity.Role.valueOf(request.getRole().toString())
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
        } catch (DatabaseUnavailableException ex) {
            responseObserver.onError(
                    Status.UNAVAILABLE
                            .withDescription(ex.getMessage())
                            .asRuntimeException()
            );
        }
    }
}