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
    @io.swagger.v3.oas.annotations.media.Schema(example = "Human Resources", description = "Name of the department")
    private String departmentName;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    @io.swagger.v3.oas.annotations.media.Schema(example = "Manages employee recruitment, payroll processing, and benefits administration.", description = "Brief description of the department's responsibilities")
    private String description;
}
