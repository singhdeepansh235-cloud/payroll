package com.srmcem.payroll.service;

import com.srmcem.payroll.dto.EmployeeRequest;
import com.srmcem.payroll.dto.EmployeeResponse;
import com.srmcem.payroll.entity.Department;
import com.srmcem.payroll.entity.Designation;
import com.srmcem.payroll.entity.Employee;
import com.srmcem.payroll.enums.EmployeeStatus;
import com.srmcem.payroll.enums.Gender;
import com.srmcem.payroll.exception.BadRequestException;
import com.srmcem.payroll.exception.ResourceNotFoundException;
import com.srmcem.payroll.repository.DepartmentRepository;
import com.srmcem.payroll.repository.DesignationRepository;
import com.srmcem.payroll.repository.EmployeeRepository;
import com.srmcem.payroll.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link EmployeeServiceImpl}.
 *
 * Covers: addEmployee, updateEmployee, deleteEmployee,
 *         getEmployeeById, getAllEmployees, searchEmployees.
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock private EmployeeRepository    employeeRepository;
    @Mock private DepartmentRepository  departmentRepository;
    @Mock private DesignationRepository designationRepository;
    @Mock private AuditLogService       auditLogService;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Department  dept;
    private Designation desg;
    private Employee    emp;
    private EmployeeRequest request;

    @BeforeEach
    void setUp() {
        dept = Department.builder()
                .departmentId(1L).departmentName("Engineering").build();
        desg = Designation.builder()
                .designationId(1L).designationName("Software Engineer").build();

        emp = Employee.builder()
                .employeeId(1L)
                .firstName("John").lastName("Doe")
                .gender(Gender.MALE)
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .email("john.doe@company.com")
                .phone("+919876543210")
                .address("123 Main St")
                .department(dept).designation(desg)
                .joiningDate(LocalDate.of(2023, 1, 10))
                .salary(new BigDecimal("75000.00"))
                .status(EmployeeStatus.ACTIVE)
                .build();

        request = new EmployeeRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setGender(Gender.MALE);
        request.setDateOfBirth("1990-05-15");
        request.setEmail("john.doe@company.com");
        request.setPhone("+919876543210");
        request.setAddress("123 Main St");
        request.setDepartmentId(1L);
        request.setDesignationId(1L);
        request.setJoiningDate("2023-01-10");
        request.setSalary(new BigDecimal("75000.00"));
    }

    // -----------------------------------------------------------------------
    // addEmployee()
    // -----------------------------------------------------------------------

    /**
     * TC-EMP-01: addEmployee() — happy path.
     * Verifies the employee is built correctly and saved, returning a valid response.
     */
    @Test
    @DisplayName("TC-EMP-01: addEmployee() — success creates employee and returns response")
    void addEmployee_success_returnsResponse() {
        when(employeeRepository.existsByEmailIgnoreCase("john.doe@company.com")).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
        when(designationRepository.findById(1L)).thenReturn(Optional.of(desg));
        when(employeeRepository.save(any(Employee.class))).thenReturn(emp);
        doNothing().when(auditLogService).log(anyString(), anyString());

        EmployeeResponse response = employeeService.addEmployee(request);

        assertThat(response).isNotNull();
        assertThat(response.getEmployeeId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("john.doe@company.com");
        assertThat(response.getDepartmentName()).isEqualTo("Engineering");

        verify(employeeRepository).save(any(Employee.class));
    }

    /**
     * TC-EMP-02: addEmployee() — duplicate email throws BadRequestException.
     * Ensures the uniqueness constraint on email is enforced before persisting.
     */
    @Test
    @DisplayName("TC-EMP-02: addEmployee() — duplicate email throws BadRequestException")
    void addEmployee_duplicateEmail_throwsBadRequestException() {
        when(employeeRepository.existsByEmailIgnoreCase("john.doe@company.com")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.addEmployee(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");

        verify(employeeRepository, never()).save(any());
    }

    /**
     * TC-EMP-03: addEmployee() — unknown department ID throws ResourceNotFoundException.
     */
    @Test
    @DisplayName("TC-EMP-03: addEmployee() — invalid departmentId throws ResourceNotFoundException")
    void addEmployee_invalidDepartment_throwsResourceNotFoundException() {
        when(employeeRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.addEmployee(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(employeeRepository, never()).save(any());
    }

    /**
     * TC-EMP-04: addEmployee() — unknown designation ID throws ResourceNotFoundException.
     */
    @Test
    @DisplayName("TC-EMP-04: addEmployee() — invalid designationId throws ResourceNotFoundException")
    void addEmployee_invalidDesignation_throwsResourceNotFoundException() {
        when(employeeRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
        when(designationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.addEmployee(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(employeeRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // updateEmployee()
    // -----------------------------------------------------------------------

    /**
     * TC-EMP-05: updateEmployee() — happy path.
     * Verifies all fields are updated and the saved entity is mapped to response.
     */
    @Test
    @DisplayName("TC-EMP-05: updateEmployee() — success updates and returns response")
    void updateEmployee_success_returnsUpdatedResponse() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        when(employeeRepository.existsByEmailIgnoreCaseAndEmployeeIdNot("john.doe@company.com", 1L))
                .thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
        when(designationRepository.findById(1L)).thenReturn(Optional.of(desg));
        when(employeeRepository.save(any(Employee.class))).thenReturn(emp);
        doNothing().when(auditLogService).log(anyString(), anyString());

        EmployeeResponse response = employeeService.updateEmployee(1L, request);

        assertThat(response).isNotNull();
        assertThat(response.getEmployeeId()).isEqualTo(1L);
        verify(employeeRepository).save(any(Employee.class));
    }

    /**
     * TC-EMP-06: updateEmployee() — employee not found throws ResourceNotFoundException.
     */
    @Test
    @DisplayName("TC-EMP-06: updateEmployee() — employee not found throws ResourceNotFoundException")
    void updateEmployee_notFound_throwsResourceNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.updateEmployee(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    /**
     * TC-EMP-07: updateEmployee() — email already used by another employee throws BadRequestException.
     */
    @Test
    @DisplayName("TC-EMP-07: updateEmployee() — conflicting email throws BadRequestException")
    void updateEmployee_emailConflict_throwsBadRequestException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        when(employeeRepository.existsByEmailIgnoreCaseAndEmployeeIdNot("john.doe@company.com", 1L))
                .thenReturn(true);

        assertThatThrownBy(() -> employeeService.updateEmployee(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already in use");
    }

    // -----------------------------------------------------------------------
    // deleteEmployee()
    // -----------------------------------------------------------------------

    /**
     * TC-EMP-08: deleteEmployee() — happy path deletes the entity.
     */
    @Test
    @DisplayName("TC-EMP-08: deleteEmployee() — employee found is deleted")
    void deleteEmployee_found_deletesEntity() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        doNothing().when(employeeRepository).delete(emp);
        doNothing().when(auditLogService).log(anyString(), anyString());

        employeeService.deleteEmployee(1L);

        verify(employeeRepository).delete(emp);
    }

    /**
     * TC-EMP-09: deleteEmployee() — unknown ID throws ResourceNotFoundException.
     */
    @Test
    @DisplayName("TC-EMP-09: deleteEmployee() — employee not found throws ResourceNotFoundException")
    void deleteEmployee_notFound_throwsResourceNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.deleteEmployee(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(employeeRepository, never()).delete(any());
    }

    // -----------------------------------------------------------------------
    // getEmployeeById()
    // -----------------------------------------------------------------------

    /**
     * TC-EMP-10: getEmployeeById() — returns correct response for a known ID.
     */
    @Test
    @DisplayName("TC-EMP-10: getEmployeeById() — returns EmployeeResponse for existing ID")
    void getEmployeeById_found_returnsResponse() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));

        EmployeeResponse response = employeeService.getEmployeeById(1L);

        assertThat(response.getEmployeeId()).isEqualTo(1L);
        assertThat(response.getFullName()).isEqualTo("John Doe");
    }

    /**
     * TC-EMP-11: getEmployeeById() — unknown ID throws ResourceNotFoundException.
     */
    @Test
    @DisplayName("TC-EMP-11: getEmployeeById() — not found throws ResourceNotFoundException")
    void getEmployeeById_notFound_throwsResourceNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -----------------------------------------------------------------------
    // getAllEmployees()
    // -----------------------------------------------------------------------

    /**
     * TC-EMP-12: getAllEmployees() — returns mapped list of all employees.
     */
    @Test
    @DisplayName("TC-EMP-12: getAllEmployees() — returns all employees as response list")
    void getAllEmployees_returnsAllEmployees() {
        when(employeeRepository.findAll()).thenReturn(List.of(emp));

        List<EmployeeResponse> responses = employeeService.getAllEmployees();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getEmail()).isEqualTo("john.doe@company.com");
    }

    // -----------------------------------------------------------------------
    // searchEmployees()
    // -----------------------------------------------------------------------

    /**
     * TC-EMP-13: searchEmployees() — blank keyword returns all employees (delegates to getAllEmployees).
     */
    @Test
    @DisplayName("TC-EMP-13: searchEmployees() — blank keyword returns all employees")
    void searchEmployees_blankKeyword_returnsAll() {
        when(employeeRepository.findAll()).thenReturn(List.of(emp));

        List<EmployeeResponse> result = employeeService.searchEmployees("  ");

        assertThat(result).hasSize(1);
        verify(employeeRepository).findAll();
        verify(employeeRepository, never()).search(anyString());
    }

    /**
     * TC-EMP-14: searchEmployees() — non-blank keyword delegates to repository search.
     */
    @Test
    @DisplayName("TC-EMP-14: searchEmployees() — keyword delegates to repository search()")
    void searchEmployees_withKeyword_delegatesToSearch() {
        when(employeeRepository.search("%John%")).thenReturn(List.of(emp));

        List<EmployeeResponse> result = employeeService.searchEmployees("John");

        assertThat(result).hasSize(1);
        verify(employeeRepository).search("%John%");
    }
}
