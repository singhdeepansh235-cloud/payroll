package com.srmcem.payroll.service;

import com.srmcem.payroll.dto.DesignationRequest;
import com.srmcem.payroll.dto.DesignationResponse;

import java.util.List;

public interface DesignationService {

    /** Creates a new designation. Throws {@code BadRequestException} if name already exists. */
    DesignationResponse addDesignation(DesignationRequest request);

    /** Updates an existing designation. Throws {@code ResourceNotFoundException} if not found. */
    DesignationResponse updateDesignation(Long designationId, DesignationRequest request);

    /** Deletes a designation by ID. Throws {@code ResourceNotFoundException} if not found. */
    void deleteDesignation(Long designationId);

    /** Fetches a single designation by ID. Throws {@code ResourceNotFoundException} if not found. */
    DesignationResponse getDesignationById(Long designationId);

    /** Returns all designations ordered by the database default. */
    List<DesignationResponse> getAllDesignations();
}
