package com.srmcem.payroll.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body used for both creating and updating a Designation.
 */
@Data
public class DesignationRequest {

    @NotBlank(message = "Designation name is required")
    @Size(max = 100, message = "Designation name must not exceed 100 characters")
    @io.swagger.v3.oas.annotations.media.Schema(example = "Senior Software Engineer", description = "Name of the designation")
    private String designationName;
}
