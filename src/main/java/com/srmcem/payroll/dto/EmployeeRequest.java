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
    @io.swagger.v3.oas.annotations.media.Schema(example = "John", description = "First name of the employee")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    @io.swagger.v3.oas.annotations.media.Schema(example = "Doe", description = "Last name of the employee")
    private String lastName;

    @NotNull(message = "Gender is required")
    @io.swagger.v3.oas.annotations.media.Schema(example = "MALE", description = "Gender of the employee")
    private Gender gender;

    @NotBlank(message = "Date of birth is required (yyyy-MM-dd)")
    @io.swagger.v3.oas.annotations.media.Schema(example = "1990-05-15", description = "Date of birth in YYYY-MM-DD format")
    private String dateOfBirth;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @io.swagger.v3.oas.annotations.media.Schema(example = "john.doe@company.com", description = "Unique email address of the employee")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+]?[0-9]{7,15}$", message = "Phone number must be 7-15 digits")
    @io.swagger.v3.oas.annotations.media.Schema(example = "+919876543210", description = "Contact number (7-15 digits)")
    private String phone;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    @io.swagger.v3.oas.annotations.media.Schema(example = "123 Main St, New York, NY", description = "Residential address")
    private String address;

    @NotNull(message = "Department ID is required")
    @io.swagger.v3.oas.annotations.media.Schema(example = "1", description = "ID of the assigned department")
    private Long departmentId;

    @NotNull(message = "Designation ID is required")
    @io.swagger.v3.oas.annotations.media.Schema(example = "1", description = "ID of the assigned designation")
    private Long designationId;

    @NotBlank(message = "Joining date is required (yyyy-MM-dd)")
    @io.swagger.v3.oas.annotations.media.Schema(example = "2023-01-10", description = "Date of joining in YYYY-MM-DD format")
    private String joiningDate;

    @NotNull(message = "Salary is required")
    @DecimalMin(value = "0.01", message = "Salary must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Salary must have at most 10 integer digits and 2 decimal places")
    @io.swagger.v3.oas.annotations.media.Schema(example = "75000.00", description = "Monthly basic/gross salary")
    private BigDecimal salary;

    /** Defaults to ACTIVE when not supplied (handled in service). */
    @io.swagger.v3.oas.annotations.media.Schema(example = "ACTIVE", description = "Status of employee (ACTIVE, INACTIVE)")
    private EmployeeStatus status;
}
