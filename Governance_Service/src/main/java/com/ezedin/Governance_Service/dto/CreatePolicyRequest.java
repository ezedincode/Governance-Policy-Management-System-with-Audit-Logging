package com.ezedin.Governance_Service.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class CreatePolicyRequest {

    @NotBlank(message = "Title is required")
    private String title;
    @NotBlank(message = "description is required")
    private String description;
    @NotBlank(message = "createdBy is required")
    private String createdBy;
}
