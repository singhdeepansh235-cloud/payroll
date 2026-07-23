package com.srmcem.payroll.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompanySettingsDto {

    private Long id;

    @NotBlank(message = "Company Name is required")
    private String companyName;

    private String address;
    private String email;
    private String phone;
    private String website;
    private String logoPath;
    private String financialYear;
}
