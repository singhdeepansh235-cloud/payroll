package com.srmcem.payroll.service;

import com.srmcem.payroll.dto.DesignationRequest;
import com.srmcem.payroll.dto.DesignationResponse;
import com.srmcem.payroll.entity.Designation;
import com.srmcem.payroll.exception.BadRequestException;
import com.srmcem.payroll.exception.ResourceNotFoundException;
import com.srmcem.payroll.repository.DesignationRepository;
import com.srmcem.payroll.service.impl.DesignationServiceImpl;
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
 * Unit tests for {@link DesignationServiceImpl}.
 *
 * Covers: addDesignation, updateDesignation, deleteDesignation,
 *         getDesignationById, getAllDesignations.
 */
@ExtendWith(MockitoExtension.class)
class DesignationServiceTest {

    @Mock private DesignationRepository designationRepository;

    @InjectMocks
    private DesignationServiceImpl designationService;

    private Designation       sampleDesignation;
    private DesignationRequest request;

    @BeforeEach
    void setUp() {
        sampleDesignation = Designation.builder()
                .designationId(1L)
                .designationName("Software Engineer")
                .build();

        request = new DesignationRequest();
        request.setDesignationName("Software Engineer");
    }

    // -----------------------------------------------------------------------
    // addDesignation()
    // -----------------------------------------------------------------------

    /**
     * TC-DESG-01: addDesignation() — success creates and returns DesignationResponse.
     * Verifies the new designation is persisted with correct name.
     */
    @Test
    @DisplayName("TC-DESG-01: addDesignation() — success creates designation and returns response")
    void addDesignation_success_returnsResponse() {
        when(designationRepository.existsByDesignationNameIgnoreCase("Software Engineer"))
                .thenReturn(false);
        when(designationRepository.save(any(Designation.class))).thenReturn(sampleDesignation);

        DesignationResponse response = designationService.addDesignation(request);

        assertThat(response).isNotNull();
        assertThat(response.getDesignationId()).isEqualTo(1L);
        assertThat(response.getDesignationName()).isEqualTo("Software Engineer");

        verify(designationRepository).save(any(Designation.class));
    }

    /**
     * TC-DESG-02: addDesignation() — duplicate name throws BadRequestException.
     * Ensures the unique constraint on designation name is enforced.
     */
    @Test
    @DisplayName("TC-DESG-02: addDesignation() — duplicate name throws BadRequestException")
    void addDesignation_duplicateName_throwsBadRequestException() {
        when(designationRepository.existsByDesignationNameIgnoreCase("Software Engineer"))
                .thenReturn(true);

        assertThatThrownBy(() -> designationService.addDesignation(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");

        verify(designationRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // updateDesignation()
    // -----------------------------------------------------------------------

    /**
     * TC-DESG-03: updateDesignation() — happy path updates name and returns response.
     */
    @Test
    @DisplayName("TC-DESG-03: updateDesignation() — success updates designation and returns response")
    void updateDesignation_success_returnsUpdatedResponse() {
        when(designationRepository.findById(1L)).thenReturn(Optional.of(sampleDesignation));
        when(designationRepository.existsByDesignationNameIgnoreCaseAndDesignationIdNot(
                "Software Engineer", 1L)).thenReturn(false);
        when(designationRepository.save(any(Designation.class))).thenReturn(sampleDesignation);

        DesignationResponse response = designationService.updateDesignation(1L, request);

        assertThat(response.getDesignationId()).isEqualTo(1L);
        verify(designationRepository).save(any(Designation.class));
    }

    /**
     * TC-DESG-04: updateDesignation() — not found throws ResourceNotFoundException.
     */
    @Test
    @DisplayName("TC-DESG-04: updateDesignation() — not found throws ResourceNotFoundException")
    void updateDesignation_notFound_throwsResourceNotFoundException() {
        when(designationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> designationService.updateDesignation(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    /**
     * TC-DESG-05: updateDesignation() — name used by another designation throws BadRequestException.
     */
    @Test
    @DisplayName("TC-DESG-05: updateDesignation() — name conflict throws BadRequestException")
    void updateDesignation_nameConflict_throwsBadRequestException() {
        when(designationRepository.findById(1L)).thenReturn(Optional.of(sampleDesignation));
        when(designationRepository.existsByDesignationNameIgnoreCaseAndDesignationIdNot(
                "Software Engineer", 1L)).thenReturn(true);

        assertThatThrownBy(() -> designationService.updateDesignation(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already in use");
    }

    // -----------------------------------------------------------------------
    // deleteDesignation()
    // -----------------------------------------------------------------------

    /**
     * TC-DESG-06: deleteDesignation() — found designation is deleted successfully.
     */
    @Test
    @DisplayName("TC-DESG-06: deleteDesignation() — found designation is deleted")
    void deleteDesignation_found_deletesEntity() {
        when(designationRepository.findById(1L)).thenReturn(Optional.of(sampleDesignation));
        doNothing().when(designationRepository).delete(sampleDesignation);

        designationService.deleteDesignation(1L);

        verify(designationRepository).delete(sampleDesignation);
    }

    /**
     * TC-DESG-07: deleteDesignation() — not found throws ResourceNotFoundException.
     */
    @Test
    @DisplayName("TC-DESG-07: deleteDesignation() — not found throws ResourceNotFoundException")
    void deleteDesignation_notFound_throwsResourceNotFoundException() {
        when(designationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> designationService.deleteDesignation(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(designationRepository, never()).delete(any());
    }

    // -----------------------------------------------------------------------
    // getDesignationById()
    // -----------------------------------------------------------------------

    /**
     * TC-DESG-08: getDesignationById() — returns correct response for known ID.
     */
    @Test
    @DisplayName("TC-DESG-08: getDesignationById() — returns response for existing designation")
    void getDesignationById_found_returnsResponse() {
        when(designationRepository.findById(1L)).thenReturn(Optional.of(sampleDesignation));

        DesignationResponse response = designationService.getDesignationById(1L);

        assertThat(response.getDesignationId()).isEqualTo(1L);
        assertThat(response.getDesignationName()).isEqualTo("Software Engineer");
    }

    /**
     * TC-DESG-09: getDesignationById() — unknown ID throws ResourceNotFoundException.
     */
    @Test
    @DisplayName("TC-DESG-09: getDesignationById() — not found throws ResourceNotFoundException")
    void getDesignationById_notFound_throwsResourceNotFoundException() {
        when(designationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> designationService.getDesignationById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -----------------------------------------------------------------------
    // getAllDesignations()
    // -----------------------------------------------------------------------

    /**
     * TC-DESG-10: getAllDesignations() — returns all designations as a list.
     */
    @Test
    @DisplayName("TC-DESG-10: getAllDesignations() — returns all designations as response list")
    void getAllDesignations_returnsAll() {
        when(designationRepository.findAll()).thenReturn(List.of(sampleDesignation));

        List<DesignationResponse> responses = designationService.getAllDesignations();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getDesignationName()).isEqualTo("Software Engineer");
    }

    /**
     * TC-DESG-11: getAllDesignations() — returns empty list when none exist.
     */
    @Test
    @DisplayName("TC-DESG-11: getAllDesignations() — empty repository returns empty list")
    void getAllDesignations_empty_returnsEmptyList() {
        when(designationRepository.findAll()).thenReturn(List.of());

        List<DesignationResponse> responses = designationService.getAllDesignations();

        assertThat(responses).isEmpty();
    }
}
