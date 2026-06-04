package com.ezedin.User_Service.grpc;

import com.ezedin.User_Service.entity.User;
import com.ezedin.User_Service.repository.UserRepository;
import com.ezedin.grpc.user.UserRequest;
import com.ezedin.grpc.user.UserResponse;
import com.ezedin.grpc.user.UserServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.Optional;


@RequiredArgsConstructor
@GrpcService
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {
    private final UserRepository userRepository;
    @Override
    public void findByUsername(
            UserRequest request,
            StreamObserver<UserResponse> responseObserver) {

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
        UserResponse response = UserResponse.newBuilder()
                .setId(foundUser.getId().toString())
                .setUsername(foundUser.getUsername())
                .setPassword(foundUser.getPassword())
                .setRole(foundUser.getRole().toString())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}