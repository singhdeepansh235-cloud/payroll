package com.srmcem.payroll.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for the change-password endpoint.
 */
@Data
public class ChangePasswordRequest {

    @NotBlank(message = "Current password is required")
    @io.swagger.v3.oas.annotations.media.Schema(example = "admin123", description = "The current password of the admin")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 6, message = "New password must be at least 6 characters")
    @io.swagger.v3.oas.annotations.media.Schema(example = "newAdminPassword123", description = "The new password for the admin, minimum 6 characters")
    private String newPassword;

    @NotBlank(message = "Confirm password is required")
    @io.swagger.v3.oas.annotations.media.Schema(example = "newAdminPassword123", description = "Confirmation of the new password")
    private String confirmPassword;
}
