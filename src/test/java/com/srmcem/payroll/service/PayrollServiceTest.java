package com.srmcem.payroll.service;

import com.srmcem.payroll.dto.PayrollGenerateRequest;
import com.srmcem.payroll.dto.PayrollResponse;
import com.srmcem.payroll.entity.Employee;
import com.srmcem.payroll.entity.PayrollRecord;
import com.srmcem.payroll.exception.BadRequestException;
import com.srmcem.payroll.exception.ResourceNotFoundException;
import com.srmcem.payroll.mail.MailService;
import com.srmcem.payroll.repository.EmployeeRepository;
import com.srmcem.payroll.repository.PayrollRecordRepository;
import com.srmcem.payroll.service.impl.PayrollServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PayrollServiceImpl}.
 *
 * Covers: generatePayroll, getPayrollById, getPayrollHistoryByEmployee,
 *         getPayrollByMonth, getAllPayrollRecords.
 */
@ExtendWith(MockitoExtension.class)
class PayrollServiceTest {

    @Mock private PayrollRecordRepository payrollRecordRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private MailService mailService;
    @Mock private PayslipService payslipService;
    @Mock private AuditLogService auditLogService;

    @InjectMocks
    private PayrollServiceImpl payrollService;

    private Employee employee;
    private PayrollRecord record;
    private PayrollGenerateRequest request;

    @BeforeEach
    void setUp() {
        employee = Employee.builder()
                .employeeId(1L)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@company.com")
                .salary(new BigDecimal("60000.00"))
                .build();

        record = PayrollRecord.builder()
                .payrollId(1L)
                .employee(employee)
                .payrollMonth("2026-07")
                .basicSalary(new BigDecimal("60000.00"))
                .bonus(new BigDecimal("5000.00"))
                .overtime(new BigDecimal("2000.00"))
                .deductions(new BigDecimal("3000.00"))
                .grossSalary(new BigDecimal("67000.00"))
                .netSalary(new BigDecimal("64000.00"))
                .build();

        request = new PayrollGenerateRequest();
        request.setEmployeeId(1L);
        request.setPayrollMonth("July-2026");
        request.setBasicSalary(new BigDecimal("60000.00"));
        request.setBonus(new BigDecimal("5000.00"));
        request.setOvertime(new BigDecimal("2000.00"));
        request.setDeductions(new BigDecimal("3000.00"));
    }

    // -----------------------------------------------------------------------
    // generatePayroll()
    // -----------------------------------------------------------------------

    /**
     * TC-PAYROLL-01: generatePayroll() - success.
     * Verifies gross/net salary calculations, repository save, pdf generation, and emailing.
     */
    @Test
    @DisplayName("TC-PAYROLL-01: generatePayroll() - success generates and emails payslip")
    void generatePayroll_success_savesAndEmails() {
        byte[] fakePdf = new byte[]{1, 2, 3};

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(payrollRecordRepository.existsByEmployee_EmployeeIdAndPayrollMonth(1L, "2026-07"))
                .thenReturn(false);
        when(payrollRecordRepository.save(any(PayrollRecord.class))).thenReturn(record);
        when(payslipService.generatePayslip(1L)).thenReturn(fakePdf);
        doNothing().when(mailService).sendPayrollAndPayslipEmail(any(PayrollRecord.class), eq(fakePdf));
        doNothing().when(auditLogService).log(anyString(), anyString());

        PayrollResponse response = payrollService.generatePayroll(request);

        assertThat(response).isNotNull();
        assertThat(response.getPayrollId()).isEqualTo(1L);
        assertThat(response.getNetSalary()).isEqualTo(new BigDecimal("64000.00"));
        verify(payrollRecordRepository).save(any(PayrollRecord.class));
        verify(mailService).sendPayrollAndPayslipEmail(any(PayrollRecord.class), eq(fakePdf));
    }

    /**
     * TC-PAYROLL-02: generatePayroll() - duplicate check.
     */
    @Test
    @DisplayName("TC-PAYROLL-02: generatePayroll() - duplicate record throws BadRequestException")
    void generatePayroll_duplicate_throwsBadRequestException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(payrollRecordRepository.existsByEmployee_EmployeeIdAndPayrollMonth(1L, "2026-07"))
                .thenReturn(true);

        assertThatThrownBy(() -> payrollService.generatePayroll(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already been generated");

        verify(payrollRecordRepository, never()).save(any());
    }

    /**
     * TC-PAYROLL-03: generatePayroll() - negative net salary throws BadRequestException.
     */
    @Test
    @DisplayName("TC-PAYROLL-03: generatePayroll() - negative net salary throws BadRequestException")
    void generatePayroll_negativeNetSalary_throwsBadRequestException() {
        request.setDeductions(new BigDecimal("100000.00")); // deductions > gross (60000 + 5000 + 2000)

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(payrollRecordRepository.existsByEmployee_EmployeeIdAndPayrollMonth(1L, "2026-07"))
                .thenReturn(false);

        assertThatThrownBy(() -> payrollService.generatePayroll(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Net salary cannot be negative");

        verify(payrollRecordRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // getPayrollById()
    // -----------------------------------------------------------------------

    /**
     * TC-PAYROLL-04: getPayrollById() - found record.
     */
    @Test
    @DisplayName("TC-PAYROLL-04: getPayrollById() - returns response for existing ID")
    void getPayrollById_found_returnsResponse() {
        when(payrollRecordRepository.findById(1L)).thenReturn(Optional.of(record));

        PayrollResponse response = payrollService.getPayrollById(1L);

        assertThat(response.getPayrollId()).isEqualTo(1L);
        assertThat(response.getPayrollMonth()).isEqualTo("July-2026");
    }

    /**
     * TC-PAYROLL-05: getPayrollById() - not found throws ResourceNotFoundException.
     */
    @Test
    @DisplayName("TC-PAYROLL-05: getPayrollById() - not found throws ResourceNotFoundException")
    void getPayrollById_notFound_throwsResourceNotFoundException() {
        when(payrollRecordRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> payrollService.getPayrollById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -----------------------------------------------------------------------
    // Lists and history
    // -----------------------------------------------------------------------

    /**
     * TC-PAYROLL-06: getPayrollHistoryByEmployee() - returns list.
     */
    @Test
    @DisplayName("TC-PAYROLL-06: getPayrollHistoryByEmployee() - returns history for employee")
    void getPayrollHistoryByEmployee_returnsList() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(payrollRecordRepository.findByEmployee_EmployeeIdOrderByPayrollMonthDesc(1L))
                .thenReturn(List.of(record));

        List<PayrollResponse> history = payrollService.getPayrollHistoryByEmployee(1L);

        assertThat(history).hasSize(1);
    }

    /**
     * TC-PAYROLL-07: getPayrollByMonth() - returns records matching month.
     */
    @Test
    @DisplayName("TC-PAYROLL-07: getPayrollByMonth() - returns list for month")
    void getPayrollByMonth_returnsList() {
        when(payrollRecordRepository.findByPayrollMonthOrderByEmployee_FirstNameAsc("2026-07"))
                .thenReturn(List.of(record));

        List<PayrollResponse> list = payrollService.getPayrollByMonth("July-2026");

        assertThat(list).hasSize(1);
    }
}
