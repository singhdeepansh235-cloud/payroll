package com.srmcem.payroll.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for the login endpoint.
 */
@Data
public class LoginRequest {

    @NotBlank(message = "Username is required")
    @io.swagger.v3.oas.annotations.media.Schema(example = "admin", description = "The username of the administrator")
    private String username;

    @NotBlank(message = "Password is required")
    @io.swagger.v3.oas.annotations.media.Schema(example = "admin123", description = "The password of the administrator")
    private String password;
}
