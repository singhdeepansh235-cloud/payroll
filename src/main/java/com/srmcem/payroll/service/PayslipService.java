package com.srmcem.payroll.service;

/**
 * Service for generating PDF payslips from payroll records.
 *
 * <p>Uses OpenPDF ({@code com.github.librepdf:openpdf}) to produce a
 * professional, downloadable payslip document.
 */
public interface PayslipService {

    /**
     * Generates a PDF payslip for the given payroll record.
     *
     * @param payrollId the ID of the {@code PayrollRecord} to render
     * @return raw PDF bytes ready to be streamed as {@code application/pdf}
     * @throws com.srmcem.payroll.exception.ResourceNotFoundException if the
     *         payroll record does not exist
     */
    byte[] generatePayslip(Long payrollId);
}
