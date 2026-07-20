package com.srmcem.payroll.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Read-only view of a Department returned to the client.
 */
@Data
@Builder
public class DepartmentResponse {

    private Long departmentId;
    private String departmentName;
    private String description;
}
