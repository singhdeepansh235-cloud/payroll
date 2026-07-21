package com.srmcem.payroll.dto;

import com.srmcem.payroll.enums.EmployeeStatus;
import com.srmcem.payroll.enums.Gender;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Read-only projection of an Employee returned to the client.
 *
 * <p>Department and Designation are embedded as IDs and names
 * so the client doesn't need additional round-trips.
 * Dates are formatted as {@code dd-MM-yyyy} strings via
 * {@link com.srmcem.payroll.util.DateUtil#format}.
 */
@Data
@Builder
public class EmployeeResponse {

    private Long employeeId;
    private String firstName;
    private String lastName;
    private String fullName;
    private Gender gender;
    private String dateOfBirth;      // formatted dd-MM-yyyy
    private String email;
    private String phone;
    private String address;

    private Long   departmentId;
    private String departmentName;

    private Long   designationId;
    private String designationName;

    private String joiningDate;      // formatted dd-MM-yyyy
    private BigDecimal salary;
    private EmployeeStatus status;
}
