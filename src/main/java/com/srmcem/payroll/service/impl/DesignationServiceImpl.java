package com.srmcem.payroll.service.impl;

import com.srmcem.payroll.dto.DesignationRequest;
import com.srmcem.payroll.dto.DesignationResponse;
import com.srmcem.payroll.entity.Designation;
import com.srmcem.payroll.exception.BadRequestException;
import com.srmcem.payroll.exception.ResourceNotFoundException;
import com.srmcem.payroll.repository.DesignationRepository;
import com.srmcem.payroll.service.DesignationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DesignationServiceImpl implements DesignationService {

    private final DesignationRepository designationRepository;

    // -----------------------------------------------------------------------
    // Add
    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public DesignationResponse addDesignation(DesignationRequest request) {
        if (designationRepository.existsByDesignationNameIgnoreCase(request.getDesignationName())) {
            throw new BadRequestException(
                    "Designation '" + request.getDesignationName() + "' already exists.");
        }

        Designation designation = Designation.builder()
                .designationName(request.getDesignationName().trim())
                .build();

        Designation saved = designationRepository.save(designation);
        log.info("Designation created: id={}, name='{}'", saved.getDesignationId(), saved.getDesignationName());
        return toResponse(saved);
    }

    // -----------------------------------------------------------------------
    // Update
    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public DesignationResponse updateDesignation(Long designationId, DesignationRequest request) {
        Designation designation = findOrThrow(designationId);

        // Check for name conflict with any OTHER designation
        if (designationRepository.existsByDesignationNameIgnoreCaseAndDesignationIdNot(
                request.getDesignationName(), designationId)) {
            throw new BadRequestException(
                    "Designation name '" + request.getDesignationName() + "' is already in use.");
        }

        designation.setDesignationName(request.getDesignationName().trim());

        Designation updated = designationRepository.save(designation);
        log.info("Designation updated: id={}, name='{}'", updated.getDesignationId(), updated.getDesignationName());
        return toResponse(updated);
    }

    // -----------------------------------------------------------------------
    // Delete
    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public void deleteDesignation(Long designationId) {
        Designation designation = findOrThrow(designationId);
        designationRepository.delete(designation);
        log.info("Designation deleted: id={}, name='{}'", designationId, designation.getDesignationName());
    }

    // -----------------------------------------------------------------------
    // View by ID
    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public DesignationResponse getDesignationById(Long designationId) {
        return toResponse(findOrThrow(designationId));
    }

    // -----------------------------------------------------------------------
    // List all
    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<DesignationResponse> getAllDesignations() {
        return designationRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Designation findOrThrow(Long designationId) {
        return designationRepository.findById(designationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Designation", "designationId", designationId));
    }

    private DesignationResponse toResponse(Designation designation) {
        return DesignationResponse.builder()
                .designationId(designation.getDesignationId())
                .designationName(designation.getDesignationName())
                .build();
    }
}
