package com.srmcem.payroll.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompanySettingsDto {

    @io.swagger.v3.oas.annotations.media.Schema(example = "1", description = "ID of settings record")
    private Long id;

    @NotBlank(message = "Company Name is required")
    @io.swagger.v3.oas.annotations.media.Schema(example = "SRMCEM College", description = "Official name of the company")
    private String companyName;

    @io.swagger.v3.oas.annotations.media.Schema(example = "Lucknow, India", description = "Office postal address")
    private String address;

    @io.swagger.v3.oas.annotations.media.Schema(example = "info@srmcem.ac.in", description = "Official email address")
    private String email;

    @io.swagger.v3.oas.annotations.media.Schema(example = "+91-522-250060", description = "Corporate phone number")
    private String phone;

    @io.swagger.v3.oas.annotations.media.Schema(example = "https://srmcem.ac.in", description = "Corporate website URL")
    private String website;

    @io.swagger.v3.oas.annotations.media.Schema(example = "uploads/logos/logo.png", description = "File storage path of company logo")
    private String logoPath;

    @io.swagger.v3.oas.annotations.media.Schema(example = "2026-2027", description = "Active financial year (e.g. 2026-2027)")
    private String financialYear;
}
