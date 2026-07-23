package com.srmcem.payroll.service.impl;

import com.srmcem.payroll.dto.DepartmentRequest;
import com.srmcem.payroll.dto.DepartmentResponse;
import com.srmcem.payroll.entity.Department;
import com.srmcem.payroll.exception.BadRequestException;
import com.srmcem.payroll.exception.ResourceNotFoundException;
import com.srmcem.payroll.repository.DepartmentRepository;
import com.srmcem.payroll.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final com.srmcem.payroll.service.AuditLogService auditLogService;

    // -----------------------------------------------------------------------
    // Add
    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public DepartmentResponse addDepartment(DepartmentRequest request) {
        if (departmentRepository.existsByDepartmentNameIgnoreCase(request.getDepartmentName())) {
            throw new BadRequestException(
                    "Department '" + request.getDepartmentName() + "' already exists.");
        }

        Department department = Department.builder()
                .departmentName(request.getDepartmentName().trim())
                .description(request.getDescription())
                .build();

        Department saved = departmentRepository.save(department);
        log.info("Department created: id={}, name='{}'", saved.getDepartmentId(), saved.getDepartmentName());
        auditLogService.log("Created Department: ID=" + saved.getDepartmentId() + ", Name=" + saved.getDepartmentName(), "Department");
        return toResponse(saved);
    }

    // -----------------------------------------------------------------------
    // Update
    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public DepartmentResponse updateDepartment(Long departmentId, DepartmentRequest request) {
        Department department = findOrThrow(departmentId);

        // Check for name conflict with any OTHER department
        if (departmentRepository.existsByDepartmentNameIgnoreCaseAndDepartmentIdNot(
                request.getDepartmentName(), departmentId)) {
            throw new BadRequestException(
                    "Department name '" + request.getDepartmentName() + "' is already in use.");
        }

        department.setDepartmentName(request.getDepartmentName().trim());
        department.setDescription(request.getDescription());

        Department updated = departmentRepository.save(department);
        log.info("Department updated: id={}, name='{}'", updated.getDepartmentId(), updated.getDepartmentName());
        auditLogService.log("Updated Department: ID=" + updated.getDepartmentId() + ", Name=" + updated.getDepartmentName(), "Department");
        return toResponse(updated);
    }

    // -----------------------------------------------------------------------
    // Delete
    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public void deleteDepartment(Long departmentId) {
        Department department = findOrThrow(departmentId);
        departmentRepository.delete(department);
        log.info("Department deleted: id={}, name='{}'", departmentId, department.getDepartmentName());
        auditLogService.log("Deleted Department: ID=" + departmentId + ", Name=" + department.getDepartmentName(), "Department");
    }

    // -----------------------------------------------------------------------
    // View by ID
    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long departmentId) {
        return toResponse(findOrThrow(departmentId));
    }

    // -----------------------------------------------------------------------
    // List all
    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Department findOrThrow(Long departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department", "departmentId", departmentId));
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<DepartmentResponse> getDepartmentsPaginated(String search, org.springframework.data.domain.Pageable pageable) {
        return departmentRepository.searchPaginated(search, pageable)
                .map(this::toResponse);
    }

    private DepartmentResponse toResponse(Department department) {
        return DepartmentResponse.builder()
                .departmentId(department.getDepartmentId())
                .departmentName(department.getDepartmentName())
                .description(department.getDescription())
                .build();
    }
}
