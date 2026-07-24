package com.srmcem.payroll.service;

import com.srmcem.payroll.dto.DepartmentRequest;
import com.srmcem.payroll.dto.DepartmentResponse;
import com.srmcem.payroll.entity.Department;
import com.srmcem.payroll.exception.BadRequestException;
import com.srmcem.payroll.exception.ResourceNotFoundException;
import com.srmcem.payroll.repository.DepartmentRepository;
import com.srmcem.payroll.service.impl.DepartmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DepartmentServiceImpl}.
 *
 * Covers: addDepartment, updateDepartment, deleteDepartment,
 *         getDepartmentById, getAllDepartments.
 */
@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock private DepartmentRepository departmentRepository;
    @Mock private AuditLogService      auditLogService;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private Department      sampleDept;
    private DepartmentRequest request;

    @BeforeEach
    void setUp() {
        sampleDept = Department.builder()
                .departmentId(1L)
                .departmentName("Engineering")
                .description("Software Engineering dept")
                .build();

        request = new DepartmentRequest();
        request.setDepartmentName("Engineering");
        request.setDescription("Software Engineering dept");
    }

    // -----------------------------------------------------------------------
    // addDepartment()
    // -----------------------------------------------------------------------

    /**
     * TC-DEPT-01: addDepartment() — success creates and returns DepartmentResponse.
     * Verifies the department is persisted and fields are mapped correctly.
     */
    @Test
    @DisplayName("TC-DEPT-01: addDepartment() — success creates department and returns response")
    void addDepartment_success_returnsDepartmentResponse() {
        when(departmentRepository.existsByDepartmentNameIgnoreCase("Engineering")).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenReturn(sampleDept);
        doNothing().when(auditLogService).log(anyString(), anyString());

        DepartmentResponse response = departmentService.addDepartment(request);

        assertThat(response).isNotNull();
        assertThat(response.getDepartmentId()).isEqualTo(1L);
        assertThat(response.getDepartmentName()).isEqualTo("Engineering");
        assertThat(response.getDescription()).isEqualTo("Software Engineering dept");

        verify(departmentRepository).save(any(Department.class));
    }

    /**
     * TC-DEPT-02: addDepartment() — duplicate name throws BadRequestException.
     * Ensures no duplicate department names are persisted.
     */
    @Test
    @DisplayName("TC-DEPT-02: addDepartment() — duplicate name throws BadRequestException")
    void addDepartment_duplicateName_throwsBadRequestException() {
        when(departmentRepository.existsByDepartmentNameIgnoreCase("Engineering")).thenReturn(true);

        assertThatThrownBy(() -> departmentService.addDepartment(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");

        verify(departmentRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // updateDepartment()
    // -----------------------------------------------------------------------

    /**
     * TC-DEPT-03: updateDepartment() — happy path updates the department correctly.
     */
    @Test
    @DisplayName("TC-DEPT-03: updateDepartment() — success updates department and returns response")
    void updateDepartment_success_returnsUpdatedResponse() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(sampleDept));
        when(departmentRepository.existsByDepartmentNameIgnoreCaseAndDepartmentIdNot("Engineering", 1L))
                .thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenReturn(sampleDept);
        doNothing().when(auditLogService).log(anyString(), anyString());

        DepartmentResponse response = departmentService.updateDepartment(1L, request);

        assertThat(response.getDepartmentId()).isEqualTo(1L);
        verify(departmentRepository).save(any(Department.class));
    }

    /**
     * TC-DEPT-04: updateDepartment() — not found throws ResourceNotFoundException.
     */
    @Test
    @DisplayName("TC-DEPT-04: updateDepartment() — not found throws ResourceNotFoundException")
    void updateDepartment_notFound_throwsResourceNotFoundException() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.updateDepartment(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    /**
     * TC-DEPT-05: updateDepartment() — name conflict throws BadRequestException.
     * Ensures a name already used by a DIFFERENT department is rejected.
     */
    @Test
    @DisplayName("TC-DEPT-05: updateDepartment() — name conflict with another dept throws BadRequestException")
    void updateDepartment_nameConflict_throwsBadRequestException() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(sampleDept));
        when(departmentRepository.existsByDepartmentNameIgnoreCaseAndDepartmentIdNot("Engineering", 1L))
                .thenReturn(true);

        assertThatThrownBy(() -> departmentService.updateDepartment(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already in use");
    }

    // -----------------------------------------------------------------------
    // deleteDepartment()
    // -----------------------------------------------------------------------

    /**
     * TC-DEPT-06: deleteDepartment() — happy path deletes the department.
     */
    @Test
    @DisplayName("TC-DEPT-06: deleteDepartment() — found department is deleted")
    void deleteDepartment_found_deletesEntity() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(sampleDept));
        doNothing().when(departmentRepository).delete(sampleDept);
        doNothing().when(auditLogService).log(anyString(), anyString());

        departmentService.deleteDepartment(1L);

        verify(departmentRepository).delete(sampleDept);
    }

    /**
     * TC-DEPT-07: deleteDepartment() — not found throws ResourceNotFoundException.
     */
    @Test
    @DisplayName("TC-DEPT-07: deleteDepartment() — not found throws ResourceNotFoundException")
    void deleteDepartment_notFound_throwsResourceNotFoundException() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.deleteDepartment(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(departmentRepository, never()).delete(any());
    }

    // -----------------------------------------------------------------------
    // getDepartmentById()
    // -----------------------------------------------------------------------

    /**
     * TC-DEPT-08: getDepartmentById() — returns correct DepartmentResponse for known ID.
     */
    @Test
    @DisplayName("TC-DEPT-08: getDepartmentById() — returns response for existing department")
    void getDepartmentById_found_returnsResponse() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(sampleDept));

        DepartmentResponse response = departmentService.getDepartmentById(1L);

        assertThat(response.getDepartmentId()).isEqualTo(1L);
        assertThat(response.getDepartmentName()).isEqualTo("Engineering");
    }

    /**
     * TC-DEPT-09: getDepartmentById() — unknown ID throws ResourceNotFoundException.
     */
    @Test
    @DisplayName("TC-DEPT-09: getDepartmentById() — not found throws ResourceNotFoundException")
    void getDepartmentById_notFound_throwsResourceNotFoundException() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.getDepartmentById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -----------------------------------------------------------------------
    // getAllDepartments()
    // -----------------------------------------------------------------------

    /**
     * TC-DEPT-10: getAllDepartments() — returns mapped list of all departments.
     */
    @Test
    @DisplayName("TC-DEPT-10: getAllDepartments() — returns all departments as response list")
    void getAllDepartments_returnsAllDepartments() {
        when(departmentRepository.findAll()).thenReturn(List.of(sampleDept));

        List<DepartmentResponse> responses = departmentService.getAllDepartments();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getDepartmentName()).isEqualTo("Engineering");
    }

    /**
     * TC-DEPT-11: getAllDepartments() — returns empty list when no departments exist.
     */
    @Test
    @DisplayName("TC-DEPT-11: getAllDepartments() — empty repository returns empty list")
    void getAllDepartments_empty_returnsEmptyList() {
        when(departmentRepository.findAll()).thenReturn(List.of());

        List<DepartmentResponse> responses = departmentService.getAllDepartments();

        assertThat(responses).isEmpty();
    }
}
