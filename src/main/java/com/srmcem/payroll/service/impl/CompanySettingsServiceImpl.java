package com.srmcem.payroll.service.impl;

import com.srmcem.payroll.dto.CompanySettingsDto;
import com.srmcem.payroll.entity.CompanySettings;
import com.srmcem.payroll.repository.CompanySettingsRepository;
import com.srmcem.payroll.service.CompanySettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanySettingsServiceImpl implements CompanySettingsService {

    private final CompanySettingsRepository settingsRepository;

    @Override
    @Transactional(readOnly = true)
    public CompanySettingsDto getSettings() {
        CompanySettings settings = fetchSingleRecord();
        return toDto(settings);
    }

    @Override
    @Transactional
    public CompanySettingsDto updateSettings(CompanySettingsDto dto) {
        CompanySettings settings = fetchSingleRecord();
        
        settings.setCompanyName(dto.getCompanyName());
        settings.setAddress(dto.getAddress());
        settings.setEmail(dto.getEmail());
        settings.setPhone(dto.getPhone());
        settings.setWebsite(dto.getWebsite());
        settings.setLogoPath(dto.getLogoPath());
        settings.setFinancialYear(dto.getFinancialYear());
        
        CompanySettings saved = settingsRepository.save(settings);
        log.info("Company settings updated: {}", saved.getCompanyName());
        return toDto(saved);
    }
    
    /**
     * Ensures only one record ever exists. If the table is empty, returns a default.
     * The DataInitializer should technically seed it, but this is a fail-safe.
     */
    private CompanySettings fetchSingleRecord() {
        return settingsRepository.findAll().stream().findFirst().orElseGet(() -> {
            CompanySettings defaultSettings = CompanySettings.builder()
                    .companyName("SRMCEM")
                    .build();
            return settingsRepository.save(defaultSettings);
        });
    }

    private CompanySettingsDto toDto(CompanySettings settings) {
        return CompanySettingsDto.builder()
                .id(settings.getId())
                .companyName(settings.getCompanyName())
                .address(settings.getAddress())
                .email(settings.getEmail())
                .phone(settings.getPhone())
                .website(settings.getWebsite())
                .logoPath(settings.getLogoPath())
                .financialYear(settings.getFinancialYear())
                .build();
    }
}
