package com.srmcem.payroll.controller;

import com.srmcem.payroll.dto.CompanySettingsDto;
import com.srmcem.payroll.response.ApiResponse;
import com.srmcem.payroll.service.CompanySettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API endpoints for Company Settings.
 */
@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class CompanySettingsController {

    private final CompanySettingsService settingsService;

    @GetMapping
    public ResponseEntity<ApiResponse<CompanySettingsDto>> getSettings() {
        CompanySettingsDto settings = settingsService.getSettings();
        return ResponseEntity.ok(ApiResponse.success("Company settings fetched successfully.", settings));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<CompanySettingsDto>> updateSettings(
            @Valid @RequestBody CompanySettingsDto dto) {
        CompanySettingsDto updated = settingsService.updateSettings(dto);
        return ResponseEntity.ok(ApiResponse.success("Company settings updated successfully.", updated));
    }
}
