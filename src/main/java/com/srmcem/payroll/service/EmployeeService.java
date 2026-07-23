package com.srmcem.payroll.service;

import com.srmcem.payroll.dto.EmployeeRequest;
import com.srmcem.payroll.dto.EmployeeResponse;

import java.util.List;

public interface EmployeeService {

    /** Creates a new employee. Throws {@code BadRequestException} if email already exists. */
    EmployeeResponse addEmployee(EmployeeRequest request);

    /**
     * Updates an existing employee.
     * Throws {@code ResourceNotFoundException} if the employee, department,
     * or designation cannot be found.
     */
    EmployeeResponse updateEmployee(Long employeeId, EmployeeRequest request);

    /** Soft-deletes by setting status to INACTIVE, or hard-deletes — see impl. */
    void deleteEmployee(Long employeeId);

    /** Fetches a single employee by ID. Throws {@code ResourceNotFoundException} if not found. */
    EmployeeResponse getEmployeeById(Long employeeId);

    /** Returns all employees. */
    List<EmployeeResponse> getAllEmployees();

    /**
     * Full-text keyword search across first name, last name, email, and phone.
     *
     * @param keyword the search term (partial match supported)
     */
    List<EmployeeResponse> searchEmployees(String keyword);

    org.springframework.data.domain.Page<EmployeeResponse> searchEmployeesPaginated(String search, org.springframework.data.domain.Pageable pageable);
}
