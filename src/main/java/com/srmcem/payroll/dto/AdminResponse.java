package com.srmcem.payroll.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Read-only view of the Admin account returned to the client after a
 * successful login.  The password hash is intentionally excluded.
 */
@Data
@Builder
public class AdminResponse {

    private Long id;
    private String username;
    private String name;
    private String email;
}
