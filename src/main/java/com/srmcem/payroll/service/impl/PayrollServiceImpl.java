package com.srmcem.payroll.service.impl;

import com.srmcem.payroll.dto.PayrollGenerateRequest;
import com.srmcem.payroll.dto.PayrollResponse;
import com.srmcem.payroll.entity.Employee;
import com.srmcem.payroll.entity.PayrollRecord;
import com.srmcem.payroll.exception.BadRequestException;
import com.srmcem.payroll.exception.ResourceNotFoundException;
import com.srmcem.payroll.repository.EmployeeRepository;
import com.srmcem.payroll.repository.PayrollRecordRepository;
import com.srmcem.payroll.service.PayrollService;
import com.srmcem.payroll.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayrollServiceImpl implements PayrollService {

    private final PayrollRecordRepository payrollRecordRepository;
    private final EmployeeRepository      employeeRepository;
    private final com.srmcem.payroll.mail.MailService mailService;
    private final com.srmcem.payroll.service.PayslipService payslipService;

    // -----------------------------------------------------------------------
    // Generate Payroll
    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public PayrollResponse generatePayroll(PayrollGenerateRequest request) {
        Employee employee = findEmployeeOrThrow(request.getEmployeeId());

        // Convert "MMMM-yyyy" → "YYYY-MM" for storage
        YearMonth period       = DateUtil.parseYearMonth(request.getPayrollMonth());
        String    monthStored  = period.toString();   // "YYYY-MM"

        // Prevent duplicate payroll for the same employee + month
        if (payrollRecordRepository.existsByEmployee_EmployeeIdAndPayrollMonth(
                employee.getEmployeeId(), monthStored)) {
            throw new BadRequestException(
                    "Payroll for employee ID " + employee.getEmployeeId()
                    + " for " + request.getPayrollMonth() + " has already been generated.");
        }

        // Use provided basicSalary or fall back to the employee's salary
        BigDecimal basicSalary = (request.getBasicSalary() != null)
                ? request.getBasicSalary()
                : employee.getSalary();

        BigDecimal bonus      = nullSafe(request.getBonus());
        BigDecimal overtime   = nullSafe(request.getOvertime());
        BigDecimal deductions = nullSafe(request.getDeductions());

        // Formula
        BigDecimal grossSalary = basicSalary.add(bonus).add(overtime);
        BigDecimal netSalary   = grossSalary.subtract(deductions);

        if (netSalary.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(
                    "Net salary cannot be negative. Please review deductions.");
        }

        PayrollRecord record = PayrollRecord.builder()
                .employee(employee)
                .payrollMonth(monthStored)
                .basicSalary(basicSalary)
                .bonus(bonus)
                .overtime(overtime)
                .deductions(deductions)
                .grossSalary(grossSalary)
                .netSalary(netSalary)
                .build();

        PayrollRecord saved = payrollRecordRepository.save(record);
        log.info("Payroll generated: id={}, employeeId={}, month={}, net={}",
                saved.getPayrollId(), employee.getEmployeeId(), monthStored, netSalary);
                
        // Generate PDF and send email asynchronously
        byte[] pdfBytes = payslipService.generatePayslip(saved.getPayrollId());
        mailService.sendPayrollAndPayslipEmail(saved, pdfBytes);
        
        return toResponse(saved);
    }

    // -----------------------------------------------------------------------
    // View by ID
    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public PayrollResponse getPayrollById(Long payrollId) {
        return toResponse(findPayrollOrThrow(payrollId));
    }

    // -----------------------------------------------------------------------
    // History by Employee
    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<PayrollResponse> getPayrollHistoryByEmployee(Long employeeId) {
        findEmployeeOrThrow(employeeId);   // validate employee exists
        return payrollRecordRepository
                .findByEmployee_EmployeeIdOrderByPayrollMonthDesc(employeeId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // All Records for a Month
    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<PayrollResponse> getPayrollByMonth(String yearMonth) {
        String monthStored = DateUtil.parseYearMonth(yearMonth).toString();  // "YYYY-MM"
        return payrollRecordRepository
                .findByPayrollMonthOrderByEmployee_FirstNameAsc(monthStored)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // All Records
    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<PayrollResponse> getAllPayrollRecords() {
        return payrollRecordRepository.findAllByOrderByPayrollMonthDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // Helpers — lookup
    // -----------------------------------------------------------------------

    private PayrollRecord findPayrollOrThrow(Long id) {
        return payrollRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PayrollRecord", "payrollId", id));
    }

    private Employee findEmployeeOrThrow(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee", "employeeId", id));
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    // -----------------------------------------------------------------------
    // Mapper
    // -----------------------------------------------------------------------

    private PayrollResponse toResponse(PayrollRecord pr) {
        // Convert stored "YYYY-MM" back to human-readable "MMMM-yyyy"
        YearMonth period = YearMonth.parse(pr.getPayrollMonth());
        String displayMonth = DateUtil.format(period);   // e.g. "July-2026"

        return PayrollResponse.builder()
                .payrollId(pr.getPayrollId())
                .employeeId(pr.getEmployee().getEmployeeId())
                .employeeName(pr.getEmployee().getFirstName() + " " + pr.getEmployee().getLastName())
                .payrollMonth(displayMonth)
                .basicSalary(pr.getBasicSalary())
                .bonus(pr.getBonus())
                .overtime(pr.getOvertime())
                .deductions(pr.getDeductions())
                .grossSalary(pr.getGrossSalary())
                .netSalary(pr.getNetSalary())
                .build();
    }
}
