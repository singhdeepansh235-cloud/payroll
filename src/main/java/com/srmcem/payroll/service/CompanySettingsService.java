package com.srmcem.payroll.service;

import com.srmcem.payroll.dto.CompanySettingsDto;

public interface CompanySettingsService {

    CompanySettingsDto getSettings();

    CompanySettingsDto updateSettings(CompanySettingsDto dto);
}
