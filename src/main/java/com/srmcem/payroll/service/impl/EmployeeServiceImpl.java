package com.srmcem.payroll.service.impl;

import com.srmcem.payroll.dto.EmployeeRequest;
import com.srmcem.payroll.dto.EmployeeResponse;
import com.srmcem.payroll.entity.Department;
import com.srmcem.payroll.entity.Designation;
import com.srmcem.payroll.entity.Employee;
import com.srmcem.payroll.enums.EmployeeStatus;
import com.srmcem.payroll.exception.BadRequestException;
import com.srmcem.payroll.exception.ResourceNotFoundException;
import com.srmcem.payroll.repository.DepartmentRepository;
import com.srmcem.payroll.repository.DesignationRepository;
import com.srmcem.payroll.repository.EmployeeRepository;
import com.srmcem.payroll.service.EmployeeService;
import com.srmcem.payroll.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository    employeeRepository;
    private final DepartmentRepository  departmentRepository;
    private final DesignationRepository designationRepository;
    private final com.srmcem.payroll.service.AuditLogService auditLogService;

    // -----------------------------------------------------------------------
    // Add
    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public EmployeeResponse addEmployee(EmployeeRequest request) {
        if (employeeRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new BadRequestException(
                    "An employee with email '" + request.getEmail() + "' already exists.");
        }

        Department  department  = findDepartmentOrThrow(request.getDepartmentId());
        Designation designation = findDesignationOrThrow(request.getDesignationId());

        Employee employee = Employee.builder()
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .gender(request.getGender())
                .dateOfBirth(DateUtil.parseDate(request.getDateOfBirth()))
                .email(request.getEmail().trim().toLowerCase())
                .phone(request.getPhone().trim())
                .address(request.getAddress())
                .department(department)
                .designation(designation)
                .joiningDate(DateUtil.parseDate(request.getJoiningDate()))
                .salary(request.getSalary())
                .status(request.getStatus() != null ? request.getStatus() : EmployeeStatus.ACTIVE)
                .build();

        Employee saved = employeeRepository.save(employee);
        log.info("Employee created: id={}, name='{} {}'",
                saved.getEmployeeId(), saved.getFirstName(), saved.getLastName());
        auditLogService.log("Created Employee: ID=" + saved.getEmployeeId() + ", Name=" + saved.getFirstName() + " " + saved.getLastName(), "Employee");
        return toResponse(saved);
    }

    // -----------------------------------------------------------------------
    // Update
    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(Long employeeId, EmployeeRequest request) {
        Employee employee = findEmployeeOrThrow(employeeId);

        // Email uniqueness check — skip if email hasn't changed
        if (employeeRepository.existsByEmailIgnoreCaseAndEmployeeIdNot(
                request.getEmail(), employeeId)) {
            throw new BadRequestException(
                    "Email '" + request.getEmail() + "' is already in use by another employee.");
        }

        Department  department  = findDepartmentOrThrow(request.getDepartmentId());
        Designation designation = findDesignationOrThrow(request.getDesignationId());

        employee.setFirstName(request.getFirstName().trim());
        employee.setLastName(request.getLastName().trim());
        employee.setGender(request.getGender());
        employee.setDateOfBirth(DateUtil.parseDate(request.getDateOfBirth()));
        employee.setEmail(request.getEmail().trim().toLowerCase());
        employee.setPhone(request.getPhone().trim());
        employee.setAddress(request.getAddress());
        employee.setDepartment(department);
        employee.setDesignation(designation);
        employee.setJoiningDate(DateUtil.parseDate(request.getJoiningDate()));
        employee.setSalary(request.getSalary());
        if (request.getStatus() != null) {
            employee.setStatus(request.getStatus());
        }

        Employee updated = employeeRepository.save(employee);
        log.info("Employee updated: id={}, name='{} {}'",
                updated.getEmployeeId(), updated.getFirstName(), updated.getLastName());
        auditLogService.log("Updated Employee: ID=" + updated.getEmployeeId() + ", Name=" + updated.getFirstName() + " " + updated.getLastName(), "Employee");
        return toResponse(updated);
    }

    // -----------------------------------------------------------------------
    // Delete
    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public void deleteEmployee(Long employeeId) {
        Employee employee = findEmployeeOrThrow(employeeId);
        employeeRepository.delete(employee);
        log.info("Employee deleted: id={}, name='{} {}'",
                employeeId, employee.getFirstName(), employee.getLastName());
        auditLogService.log("Deleted Employee: ID=" + employeeId + ", Name=" + employee.getFirstName() + " " + employee.getLastName(), "Employee");
    }

    // -----------------------------------------------------------------------
    // View by ID
    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long employeeId) {
        return toResponse(findEmployeeOrThrow(employeeId));
    }

    // -----------------------------------------------------------------------
    // List all
    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // Search
    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> searchEmployees(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllEmployees();
        }
        String wildcardKw = "%" + keyword.trim() + "%";
        return employeeRepository.search(wildcardKw)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // Helpers — lookup
    // -----------------------------------------------------------------------

    private Employee findEmployeeOrThrow(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "employeeId", id));
    }

    private Department findDepartmentOrThrow(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "departmentId", id));
    }

    private Designation findDesignationOrThrow(Long id) {
        return designationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Designation", "designationId", id));
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<EmployeeResponse> searchEmployeesPaginated(String search, org.springframework.data.domain.Pageable pageable) {
        return employeeRepository.searchPaginated(search, pageable)
                .map(this::toResponse);
    }

    // -----------------------------------------------------------------------
    // Mapper
    // -----------------------------------------------------------------------

    private EmployeeResponse toResponse(Employee e) {
        return EmployeeResponse.builder()
                .employeeId(e.getEmployeeId())
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .fullName(e.getFirstName() + " " + e.getLastName())
                .gender(e.getGender())
                .dateOfBirth(DateUtil.format(e.getDateOfBirth()))
                .email(e.getEmail())
                .phone(e.getPhone())
                .address(e.getAddress())
                .departmentId(e.getDepartment().getDepartmentId())
                .departmentName(e.getDepartment().getDepartmentName())
                .designationId(e.getDesignation().getDesignationId())
                .designationName(e.getDesignation().getDesignationName())
                .joiningDate(DateUtil.format(e.getJoiningDate()))
                .salary(e.getSalary())
                .status(e.getStatus())
                .build();
    }
}
