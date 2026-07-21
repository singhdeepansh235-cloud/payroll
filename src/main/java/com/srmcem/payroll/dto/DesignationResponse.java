package com.srmcem.payroll.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Read-only view of a Designation returned to the client.
 */
@Data
@Builder
public class DesignationResponse {

    private Long designationId;
    private String designationName;
}
