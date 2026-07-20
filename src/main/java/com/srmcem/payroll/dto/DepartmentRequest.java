package com.srmcem.payroll.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body used for both creating and updating a Department.
 */
@Data
public class DepartmentRequest {

    @NotBlank(message = "Department name is required")
    @Size(max = 100, message = "Department name must not exceed 100 characters")
    private String departmentName;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
}
