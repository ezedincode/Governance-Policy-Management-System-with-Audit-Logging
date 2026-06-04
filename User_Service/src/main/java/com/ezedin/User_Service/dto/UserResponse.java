package com.ezedin.User_Service.dto;

import com.ezedin.User_Service.entity.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserResponse {

    private UUID id;
    private String username;
    private String email;
    private Role role;
    private LocalDateTime createdAt;
}
