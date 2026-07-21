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
    private String designationName;
}
