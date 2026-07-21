package com.srmcem.payroll.dto;

import com.srmcem.payroll.enums.EmployeeStatus;
import com.srmcem.payroll.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request body used for both creating and updating an Employee.
 *
 * <p>Dates are accepted as ISO strings ({@code "yyyy-MM-dd"}) and parsed in
 * the service layer using {@link com.srmcem.payroll.util.DateUtil#parseDate}.
 */
@Data
public class EmployeeRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotBlank(message = "Date of birth is required (yyyy-MM-dd)")
    private String dateOfBirth;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+]?[0-9]{7,15}$", message = "Phone number must be 7-15 digits")
    private String phone;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @NotNull(message = "Designation ID is required")
    private Long designationId;

    @NotBlank(message = "Joining date is required (yyyy-MM-dd)")
    private String joiningDate;

    @NotNull(message = "Salary is required")
    @DecimalMin(value = "0.01", message = "Salary must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Salary must have at most 10 integer digits and 2 decimal places")
    private BigDecimal salary;

    /** Defaults to ACTIVE when not supplied (handled in service). */
    private EmployeeStatus status;
}
