package com.srmcem.payroll.payroll;

import com.srmcem.payroll.dto.PayrollGenerateRequest;
import com.srmcem.payroll.dto.PayrollResponse;
import com.srmcem.payroll.entity.Department;
import com.srmcem.payroll.entity.Designation;
import com.srmcem.payroll.entity.Employee;
import com.srmcem.payroll.entity.PayrollRecord;
import com.srmcem.payroll.enums.EmployeeStatus;
import com.srmcem.payroll.enums.Gender;
import com.srmcem.payroll.exception.BadRequestException;
import com.srmcem.payroll.exception.ResourceNotFoundException;
import com.srmcem.payroll.repository.EmployeeRepository;
import com.srmcem.payroll.repository.PayrollRecordRepository;
import com.srmcem.payroll.service.impl.PayrollServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Unit tests for PayrollServiceImpl.
 * All dependencies are mocked - no database required.
 */
@ExtendWith(MockitoExtension.class)
class PayrollServiceImplTest {

    @Mock
    private PayrollRecordRepository payrollRecordRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private PayrollServiceImpl payrollService;

    private Employee employee;
    private PayrollRecord savedRecord;

    @BeforeEach
    void setUpFixtures() {
        Department dept = Department.builder()
                .departmentId(1L)
                .departmentName("Engineering")
                .build();

        Designation desig = Designation.builder()
                .designationId(1L)
                .designationName("Software Engineer")
                .build();

        employee = Employee.builder()
                .employeeId(10L)
                .firstName("Bob")
                .lastName("Jones")
                .email("bob@example.com")
                .phone("9876543210")
                .gender(Gender.MALE)
                .dateOfBirth(LocalDate.of(1990, 3, 20))
                .joiningDate(LocalDate.of(2019, 6, 1))
                .salary(new BigDecimal("50000.00"))
                .status(EmployeeStatus.ACTIVE)
                .department(dept)
                .designation(desig)
                .build();

        savedRecord = PayrollRecord.builder()
                .payrollId(1L)
                .employee(employee)
                .payrollMonth("2026-07")
                .basicSalary(new BigDecimal("50000.00"))
                .bonus(new BigDecimal("5000.00"))
                .overtime(new BigDecimal("2000.00"))
                .deductions(new BigDecimal("8000.00"))
                .grossSalary(new BigDecimal("57000.00"))
                .netSalary(new BigDecimal("49000.00"))
                .build();
    }

    // =========================================================================
    // 1. Generate Payroll
    // =========================================================================

    @Nested
    @DisplayName("generatePayroll()")
    class GeneratePayroll {

        private PayrollGenerateRequest fullRequest() {
            PayrollGenerateRequest req = new PayrollGenerateRequest();
            req.setEmployeeId(10L);
            req.setPayrollMonth("July-2026");
            req.setBasicSalary(new BigDecimal("50000.00"));
            req.setBonus(new BigDecimal("5000.00"));
            req.setOvertime(new BigDecimal("2000.00"));
            req.setDeductions(new BigDecimal("8000.00"));
            return req;
        }

        @Test
        @DisplayName("saves and returns PayrollResponse with correct formula values")
        void generatePayroll_validInput_returnsResponse() {
            given(employeeRepository.findById(10L)).willReturn(Optional.of(employee));
            given(payrollRecordRepository.existsByEmployee_EmployeeIdAndPayrollMonth(10L, "2026-07"))
                    .willReturn(false);
            given(payrollRecordRepository.save(any(PayrollRecord.class))).willReturn(savedRecord);

            PayrollResponse response = payrollService.generatePayroll(fullRequest());

            assertThat(response).isNotNull();
            assertThat(response.getPayrollId()).isEqualTo(1L);
            assertThat(response.getEmployeeId()).isEqualTo(10L);
            assertThat(response.getEmployeeName()).isEqualTo("Bob Jones");
            assertThat(response.getPayrollMonth()).isEqualTo("July-2026");
            assertThat(response.getBasicSalary()).isEqualByComparingTo("50000.00");
            assertThat(response.getBonus()).isEqualByComparingTo("5000.00");
            assertThat(response.getOvertime()).isEqualByComparingTo("2000.00");
            assertThat(response.getDeductions()).isEqualByComparingTo("8000.00");
            assertThat(response.getGrossSalary()).isEqualByComparingTo("57000.00");
            assertThat(response.getNetSalary()).isEqualByComparingTo("49000.00");

            then(payrollRecordRepository).should().save(any(PayrollRecord.class));
        }

        @Test
        @DisplayName("uses employee salary as basicSalary when none provided in request")
        void generatePayroll_noBasicSalaryProvided_usesEmployeeSalary() {
            PayrollGenerateRequest req = new PayrollGenerateRequest();
            req.setEmployeeId(10L);
            req.setPayrollMonth("July-2026");
            // basicSalary = null -> falls back to employee.getSalary() = 50000

            PayrollRecord noOverrideRecord = PayrollRecord.builder()
                    .payrollId(2L)
                    .employee(employee)
                    .payrollMonth("2026-07")
                    .basicSalary(new BigDecimal("50000.00"))
                    .bonus(BigDecimal.ZERO)
                    .overtime(BigDecimal.ZERO)
                    .deductions(BigDecimal.ZERO)
                    .grossSalary(new BigDecimal("50000.00"))
                    .netSalary(new BigDecimal("50000.00"))
                    .build();

            given(employeeRepository.findById(10L)).willReturn(Optional.of(employee));
            given(payrollRecordRepository.existsByEmployee_EmployeeIdAndPayrollMonth(10L, "2026-07"))
                    .willReturn(false);
            given(payrollRecordRepository.save(any())).willReturn(noOverrideRecord);

            PayrollResponse response = payrollService.generatePayroll(req);

            assertThat(response.getBasicSalary()).isEqualByComparingTo("50000.00");
            assertThat(response.getGrossSalary()).isEqualByComparingTo("50000.00");
            assertThat(response.getNetSalary()).isEqualByComparingTo("50000.00");
        }

        @Test
        @DisplayName("null bonus/overtime/deductions default to zero")
        void generatePayroll_nullOptionalFields_defaultToZero() {
            PayrollGenerateRequest req = new PayrollGenerateRequest();
            req.setEmployeeId(10L);
            req.setPayrollMonth("July-2026");
            req.setBasicSalary(new BigDecimal("50000.00"));
            // bonus, overtime, deductions left null

            PayrollRecord zeroExtrasRecord = PayrollRecord.builder()
                    .payrollId(3L)
                    .employee(employee)
                    .payrollMonth("2026-07")
                    .basicSalary(new BigDecimal("50000.00"))
                    .bonus(BigDecimal.ZERO)
                    .overtime(BigDecimal.ZERO)
                    .deductions(BigDecimal.ZERO)
                    .grossSalary(new BigDecimal("50000.00"))
                    .netSalary(new BigDecimal("50000.00"))
                    .build();

            given(employeeRepository.findById(10L)).willReturn(Optional.of(employee));
            given(payrollRecordRepository.existsByEmployee_EmployeeIdAndPayrollMonth(10L, "2026-07"))
                    .willReturn(false);
            given(payrollRecordRepository.save(any())).willReturn(zeroExtrasRecord);

            PayrollResponse response = payrollService.generatePayroll(req);

            assertThat(response.getBonus()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(response.getOvertime()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(response.getDeductions()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("grossSalary = basicSalary + bonus + overtime (formula check)")
        void generatePayroll_formulaGrossSalary_isCorrect() {
            // basic=60000, bonus=3000, overtime=1500 -> gross=64500
            PayrollGenerateRequest req = fullRequest();
            req.setBasicSalary(new BigDecimal("60000.00"));
            req.setBonus(new BigDecimal("3000.00"));
            req.setOvertime(new BigDecimal("1500.00"));
            req.setDeductions(new BigDecimal("5000.00"));

            PayrollRecord formulaRecord = PayrollRecord.builder()
                    .payrollId(4L)
                    .employee(employee)
                    .payrollMonth("2026-07")
                    .basicSalary(new BigDecimal("60000.00"))
                    .bonus(new BigDecimal("3000.00"))
                    .overtime(new BigDecimal("1500.00"))
                    .deductions(new BigDecimal("5000.00"))
                    .grossSalary(new BigDecimal("64500.00"))
                    .netSalary(new BigDecimal("59500.00"))
                    .build();

            given(employeeRepository.findById(10L)).willReturn(Optional.of(employee));
            given(payrollRecordRepository.existsByEmployee_EmployeeIdAndPayrollMonth(10L, "2026-07"))
                    .willReturn(false);
            given(payrollRecordRepository.save(any())).willReturn(formulaRecord);

            PayrollResponse response = payrollService.generatePayroll(req);

            assertThat(response.getGrossSalary()).isEqualByComparingTo("64500.00");
            assertThat(response.getNetSalary()).isEqualByComparingTo("59500.00");
        }

        @Test
        @DisplayName("netSalary = grossSalary - deductions (formula check)")
        void generatePayroll_formulaNetSalary_isCorrect() {
            // gross=57000, deductions=8000 -> net=49000
            given(employeeRepository.findById(10L)).willReturn(Optional.of(employee));
            given(payrollRecordRepository.existsByEmployee_EmployeeIdAndPayrollMonth(10L, "2026-07"))
                    .willReturn(false);
            given(payrollRecordRepository.save(any())).willReturn(savedRecord);

            PayrollResponse response = payrollService.generatePayroll(fullRequest());

            assertThat(response.getNetSalary())
                    .isEqualByComparingTo(
                        response.getGrossSalary().subtract(response.getDeductions()));
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when employee does not exist")
        void generatePayroll_unknownEmployee_throws404() {
            given(employeeRepository.findById(99L)).willReturn(Optional.empty());

            PayrollGenerateRequest req = fullRequest();
            req.setEmployeeId(99L);

            assertThatThrownBy(() -> payrollService.generatePayroll(req))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("throws BadRequestException when payroll already generated for that month")
        void generatePayroll_duplicate_throws400() {
            given(employeeRepository.findById(10L)).willReturn(Optional.of(employee));
            given(payrollRecordRepository.existsByEmployee_EmployeeIdAndPayrollMonth(10L, "2026-07"))
                    .willReturn(true);

            assertThatThrownBy(() -> payrollService.generatePayroll(fullRequest()))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("already been generated");
        }

        @Test
        @DisplayName("throws BadRequestException when deductions exceed gross salary (negative net)")
        void generatePayroll_deductionsExceedGross_throws400() {
            // basic=50000, bonus=0, overtime=0, deductions=60000 -> net=-10000
            PayrollGenerateRequest req = new PayrollGenerateRequest();
            req.setEmployeeId(10L);
            req.setPayrollMonth("July-2026");
            req.setBasicSalary(new BigDecimal("50000.00"));
            req.setDeductions(new BigDecimal("60000.00"));

            given(employeeRepository.findById(10L)).willReturn(Optional.of(employee));
            given(payrollRecordRepository.existsByEmployee_EmployeeIdAndPayrollMonth(10L, "2026-07"))
                    .willReturn(false);

            assertThatThrownBy(() -> payrollService.generatePayroll(req))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Net salary cannot be negative");
        }

        @Test
        @DisplayName("generates payroll for different months independently")
        void generatePayroll_differentMonths_noDuplicateConflict() {
            PayrollRecord augustRecord = PayrollRecord.builder()
                    .payrollId(5L)
                    .employee(employee)
                    .payrollMonth("2026-08")
                    .basicSalary(new BigDecimal("50000.00"))
                    .bonus(BigDecimal.ZERO)
                    .overtime(BigDecimal.ZERO)
                    .deductions(BigDecimal.ZERO)
                    .grossSalary(new BigDecimal("50000.00"))
                    .netSalary(new BigDecimal("50000.00"))
                    .build();

            PayrollGenerateRequest req = new PayrollGenerateRequest();
            req.setEmployeeId(10L);
            req.setPayrollMonth("August-2026");
            req.setBasicSalary(new BigDecimal("50000.00"));

            given(employeeRepository.findById(10L)).willReturn(Optional.of(employee));
            given(payrollRecordRepository.existsByEmployee_EmployeeIdAndPayrollMonth(10L, "2026-08"))
                    .willReturn(false);
            given(payrollRecordRepository.save(any())).willReturn(augustRecord);

            PayrollResponse response = payrollService.generatePayroll(req);

            assertThat(response.getPayrollMonth()).isEqualTo("August-2026");
        }
    }

    // =========================================================================
    // 2. View Payroll (getPayrollById)
    // =========================================================================

    @Nested
    @DisplayName("getPayrollById()")
    class ViewPayroll {

        @Test
        @DisplayName("returns PayrollResponse for a valid payrollId")
        void getPayrollById_validId_returnsResponse() {
            given(payrollRecordRepository.findById(1L)).willReturn(Optional.of(savedRecord));

            PayrollResponse response = payrollService.getPayrollById(1L);

            assertThat(response.getPayrollId()).isEqualTo(1L);
            assertThat(response.getEmployeeId()).isEqualTo(10L);
            assertThat(response.getEmployeeName()).isEqualTo("Bob Jones");
            assertThat(response.getGrossSalary()).isEqualByComparingTo("57000.00");
            assertThat(response.getNetSalary()).isEqualByComparingTo("49000.00");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException for unknown payrollId")
        void getPayrollById_unknownId_throws404() {
            given(payrollRecordRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> payrollService.getPayrollById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("payrollMonth is returned in MMMM-yyyy format not YYYY-MM")
        void getPayrollById_monthFormat_isHumanReadable() {
            given(payrollRecordRepository.findById(1L)).willReturn(Optional.of(savedRecord));

            PayrollResponse response = payrollService.getPayrollById(1L);

            // stored as "2026-07", must be returned as "July-2026"
            assertThat(response.getPayrollMonth()).isEqualTo("July-2026");
            // confirm it is not the raw stored "YYYY-MM" format (starts with a digit)
            assertThat(response.getPayrollMonth()).matches("[A-Za-z].*");
        }
    }

    // =========================================================================
    // 3. Payroll History (getPayrollHistoryByEmployee)
    // =========================================================================

    @Nested
    @DisplayName("getPayrollHistoryByEmployee()")
    class PayrollHistory {

        @Test
        @DisplayName("returns full history for employee newest-first")
        void getHistory_validEmployee_returnsAllRecords() {
            PayrollRecord juneRecord = PayrollRecord.builder()
                    .payrollId(2L)
                    .employee(employee)
                    .payrollMonth("2026-06")
                    .basicSalary(new BigDecimal("50000.00"))
                    .bonus(BigDecimal.ZERO)
                    .overtime(BigDecimal.ZERO)
                    .deductions(new BigDecimal("5000.00"))
                    .grossSalary(new BigDecimal("50000.00"))
                    .netSalary(new BigDecimal("45000.00"))
                    .build();

            given(employeeRepository.findById(10L)).willReturn(Optional.of(employee));
            given(payrollRecordRepository.findByEmployee_EmployeeIdOrderByPayrollMonthDesc(10L))
                    .willReturn(List.of(savedRecord, juneRecord));

            List<PayrollResponse> history = payrollService.getPayrollHistoryByEmployee(10L);

            assertThat(history).hasSize(2);
            assertThat(history).extracting(PayrollResponse::getPayrollId)
                    .containsExactly(1L, 2L);
        }

        @Test
        @DisplayName("returns empty list when employee has no payroll records")
        void getHistory_noRecords_returnsEmptyList() {
            given(employeeRepository.findById(10L)).willReturn(Optional.of(employee));
            given(payrollRecordRepository.findByEmployee_EmployeeIdOrderByPayrollMonthDesc(10L))
                    .willReturn(List.of());

            List<PayrollResponse> history = payrollService.getPayrollHistoryByEmployee(10L);

            assertThat(history).isEmpty();
        }

        @Test
        @DisplayName("throws ResourceNotFoundException for unknown employeeId")
        void getHistory_unknownEmployee_throws404() {
            given(employeeRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> payrollService.getPayrollHistoryByEmployee(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("each record in history has correct gross/net salary values")
        void getHistory_recordValues_areCorrect() {
            given(employeeRepository.findById(10L)).willReturn(Optional.of(employee));
            given(payrollRecordRepository.findByEmployee_EmployeeIdOrderByPayrollMonthDesc(10L))
                    .willReturn(List.of(savedRecord));

            List<PayrollResponse> history = payrollService.getPayrollHistoryByEmployee(10L);

            PayrollResponse record = history.get(0);
            assertThat(record.getGrossSalary()).isEqualByComparingTo("57000.00");
            assertThat(record.getNetSalary()).isEqualByComparingTo("49000.00");
        }
    }

    // =========================================================================
    // 4. Payroll by Month (getPayrollByMonth)
    // =========================================================================

    @Nested
    @DisplayName("getPayrollByMonth()")
    class PayrollByMonth {

        @Test
        @DisplayName("returns all records for a given month")
        void getPayrollByMonth_validMonth_returnsRecords() {
            given(payrollRecordRepository.findByPayrollMonthOrderByEmployee_FirstNameAsc("2026-07"))
                    .willReturn(List.of(savedRecord));

            List<PayrollResponse> records = payrollService.getPayrollByMonth("July-2026");

            assertThat(records).hasSize(1);
            assertThat(records.get(0).getPayrollMonth()).isEqualTo("July-2026");
        }

        @Test
        @DisplayName("returns empty list when no payroll generated for that month")
        void getPayrollByMonth_noRecords_returnsEmptyList() {
            given(payrollRecordRepository.findByPayrollMonthOrderByEmployee_FirstNameAsc("2026-05"))
                    .willReturn(List.of());

            List<PayrollResponse> records = payrollService.getPayrollByMonth("May-2026");

            assertThat(records).isEmpty();
        }

        @Test
        @DisplayName("converts MMMM-yyyy input to YYYY-MM for the repository query")
        void getPayrollByMonth_inputConversion_correctStoredFormat() {
            given(payrollRecordRepository.findByPayrollMonthOrderByEmployee_FirstNameAsc("2026-07"))
                    .willReturn(List.of(savedRecord));

            payrollService.getPayrollByMonth("July-2026");

            // verify the correct stored format "2026-07" was used
            then(payrollRecordRepository).should()
                    .findByPayrollMonthOrderByEmployee_FirstNameAsc("2026-07");
        }
    }

    // =========================================================================
    // 5. All Payroll Records (getAllPayrollRecords)
    // =========================================================================

    @Nested
    @DisplayName("getAllPayrollRecords()")
    class AllPayrollRecords {

        @Test
        @DisplayName("returns all records across all employees newest-first")
        void getAllPayrollRecords_returnsAll() {
            given(payrollRecordRepository.findAllByOrderByPayrollMonthDesc())
                    .willReturn(List.of(savedRecord));

            List<PayrollResponse> all = payrollService.getAllPayrollRecords();

            assertThat(all).hasSize(1);
            assertThat(all.get(0).getPayrollId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("returns empty list when no records exist")
        void getAllPayrollRecords_noRecords_returnsEmptyList() {
            given(payrollRecordRepository.findAllByOrderByPayrollMonthDesc())
                    .willReturn(List.of());

            List<PayrollResponse> all = payrollService.getAllPayrollRecords();

            assertThat(all).isEmpty();
        }

        @Test
        @DisplayName("each response contains correct employeeName (firstName + lastName)")
        void getAllPayrollRecords_employeeNameFormat_isCorrect() {
            given(payrollRecordRepository.findAllByOrderByPayrollMonthDesc())
                    .willReturn(List.of(savedRecord));

            List<PayrollResponse> all = payrollService.getAllPayrollRecords();

            assertThat(all.get(0).getEmployeeName()).isEqualTo("Bob Jones");
        }
    }
}