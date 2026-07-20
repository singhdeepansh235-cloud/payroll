package com.srmcem.payroll.service;

import com.srmcem.payroll.dto.DepartmentRequest;
import com.srmcem.payroll.dto.DepartmentResponse;

import java.util.List;

public interface DepartmentService {

    /** Creates a new department. Throws {@code BadRequestException} if name already exists. */
    DepartmentResponse addDepartment(DepartmentRequest request);

    /** Updates an existing department. Throws {@code ResourceNotFoundException} if not found. */
    DepartmentResponse updateDepartment(Long departmentId, DepartmentRequest request);

    /** Deletes a department by ID. Throws {@code ResourceNotFoundException} if not found. */
    void deleteDepartment(Long departmentId);

    /** Fetches a single department by ID. Throws {@code ResourceNotFoundException} if not found. */
    DepartmentResponse getDepartmentById(Long departmentId);

    /** Returns all departments ordered by name. */
    List<DepartmentResponse> getAllDepartments();
}
