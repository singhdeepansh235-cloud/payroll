package com.srmcem.payroll.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for the login endpoint.
 */
@Data
public class LoginRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
