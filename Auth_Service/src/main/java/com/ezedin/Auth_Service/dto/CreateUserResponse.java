package com.ezedin.Auth_Service.dto;


import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CreateUserResponse {

    private String username;

    private String email;

    private Role role;
}
