package com.srmcem.payroll.service.impl;

import com.srmcem.payroll.dto.AttendanceRequest;
import com.srmcem.payroll.dto.AttendanceResponse;
import com.srmcem.payroll.dto.MonthlyAttendanceResponse;
import com.srmcem.payroll.entity.Attendance;
import com.srmcem.payroll.entity.Employee;
import com.srmcem.payroll.enums.AttendanceStatus;
import com.srmcem.payroll.exception.BadRequestException;
import com.srmcem.payroll.exception.ResourceNotFoundException;
import com.srmcem.payroll.repository.AttendanceRepository;
import com.srmcem.payroll.repository.EmployeeRepository;
import com.srmcem.payroll.service.AttendanceService;
import com.srmcem.payroll.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository   employeeRepository;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    // -----------------------------------------------------------------------
    // Mark Attendance
    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public AttendanceResponse markAttendance(AttendanceRequest request) {
        Employee employee = findEmployeeOrThrow(request.getEmployeeId());
        LocalDate date = resolveDate(request.getDate());

        if (attendanceRepository.existsByEmployee_EmployeeIdAndDate(
                employee.getEmployeeId(), date)) {
            throw new BadRequestException(
                    "Attendance for employee ID " + employee.getEmployeeId()
                    + " on " + DateUtil.format(date) + " is already marked.");
        }

        LocalTime checkIn  = parseTime(request.getCheckIn());
        LocalTime checkOut = parseTime(request.getCheckOut());
        Double workingHours = computeWorkingHours(checkIn, checkOut);

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .date(date)
                .checkIn(checkIn)
                .checkOut(checkOut)
                .workingHours(workingHours)
                .attendanceStatus(request.getAttendanceStatus())
                .build();

        Attendance saved = attendanceRepository.save(attendance);
        log.info("Attendance marked: employeeId={}, date={}, status={}",
                employee.getEmployeeId(), date, saved.getAttendanceStatus());
        return toResponse(saved);
    }

    // -----------------------------------------------------------------------
    // Update Attendance
    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public AttendanceResponse updateAttendance(Long attendanceId, AttendanceRequest request) {
        Attendance attendance = findAttendanceOrThrow(attendanceId);

        LocalTime checkIn  = parseTime(request.getCheckIn());
        LocalTime checkOut = parseTime(request.getCheckOut());
        Double workingHours = computeWorkingHours(checkIn, checkOut);

        attendance.setCheckIn(checkIn);
        attendance.setCheckOut(checkOut);
        attendance.setWorkingHours(workingHours);
        attendance.setAttendanceStatus(request.getAttendanceStatus());

        // Allow re-assigning date if provided
        if (request.getDate() != null && !request.getDate().isBlank()) {
            attendance.setDate(resolveDate(request.getDate()));
        }

        Attendance updated = attendanceRepository.save(attendance);
        log.info("Attendance updated: id={}, employeeId={}, date={}, status={}",
                attendanceId, updated.getEmployee().getEmployeeId(),
                updated.getDate(), updated.getAttendanceStatus());
        return toResponse(updated);
    }

    // -----------------------------------------------------------------------
    // View by ID
    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public AttendanceResponse getAttendanceById(Long attendanceId) {
        return toResponse(findAttendanceOrThrow(attendanceId));
    }

    // -----------------------------------------------------------------------
    // List by Employee
    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAttendanceByEmployee(Long employeeId) {
        findEmployeeOrThrow(employeeId);   // validate employee exists
        return attendanceRepository
                .findByEmployee_EmployeeIdOrderByDateDesc(employeeId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // List by Date (daily roll-call)
    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAttendanceByDate(String date) {
        LocalDate localDate = (date == null || date.isBlank())
                ? LocalDate.now()
                : DateUtil.parseDate(date);
        return attendanceRepository
                .findByDateOrderByEmployee_FirstNameAsc(localDate)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // Monthly Attendance Report
    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public MonthlyAttendanceResponse getMonthlyAttendance(Long employeeId, String yearMonth) {
        Employee employee = findEmployeeOrThrow(employeeId);
        YearMonth period  = DateUtil.parseYearMonth(yearMonth);

        LocalDate startDate = period.atDay(1);
        LocalDate endDate   = period.atEndOfMonth();

        // Day-by-day records
        List<AttendanceResponse> records = attendanceRepository
                .findByEmployee_EmployeeIdAndDateBetweenOrderByDateAsc(
                        employeeId, startDate, endDate)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        // Status summary — count per AttendanceStatus
        List<Object[]> rawCounts = attendanceRepository
                .countByStatusForEmployee(employeeId, startDate, endDate);

        Map<String, Long> statusSummary = new LinkedHashMap<>();
        // Pre-fill all statuses with 0 so the client always sees every key
        for (AttendanceStatus s : AttendanceStatus.values()) {
            statusSummary.put(s.name(), 0L);
        }
        for (Object[] row : rawCounts) {
            AttendanceStatus status = (AttendanceStatus) row[0];
            Long count = (Long) row[1];
            statusSummary.put(status.name(), count);
        }

        return MonthlyAttendanceResponse.builder()
                .employeeId(employeeId)
                .employeeName(employee.getFirstName() + " " + employee.getLastName())
                .period(DateUtil.format(period))
                .statusSummary(statusSummary)
                .records(records)
                .build();
    }

    // -----------------------------------------------------------------------
    // Helpers — lookup
    // -----------------------------------------------------------------------

    private Attendance findAttendanceOrThrow(Long id) {
        return attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Attendance", "attendanceId", id));
    }

    private Employee findEmployeeOrThrow(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee", "employeeId", id));
    }

    // -----------------------------------------------------------------------
    // Helpers — date / time
    // -----------------------------------------------------------------------

    /** Returns today when the string is null or blank, otherwise parses ISO date. */
    private LocalDate resolveDate(String dateStr) {
        return (dateStr == null || dateStr.isBlank())
                ? LocalDate.now()
                : DateUtil.parseDate(dateStr);
    }

    /** Parses HH:mm or HH:mm:ss; returns null when input is null/blank. */
    private LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.isBlank()) return null;
        return LocalTime.parse(timeStr.trim());
    }

    /**
     * Computes decimal working hours from check-in and check-out.
     * Returns null when either time is missing.
     */
    private Double computeWorkingHours(LocalTime checkIn, LocalTime checkOut) {
        if (checkIn == null || checkOut == null) return null;
        long minutes = java.time.Duration.between(checkIn, checkOut).toMinutes();
        if (minutes <= 0) return null;
        // Round to 2 decimal places
        return Math.round((minutes / 60.0) * 100.0) / 100.0;
    }

    // -----------------------------------------------------------------------
    // Mapper
    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<AttendanceResponse> getAttendancePaginated(String search, org.springframework.data.domain.Pageable pageable) {
        return attendanceRepository.searchPaginated(search, pageable)
                .map(this::toResponse);
    }

    private AttendanceResponse toResponse(Attendance a) {
        return AttendanceResponse.builder()
                .attendanceId(a.getAttendanceId())
                .employeeId(a.getEmployee().getEmployeeId())
                .employeeName(a.getEmployee().getFirstName() + " " + a.getEmployee().getLastName())
                .date(DateUtil.format(a.getDate()))
                .checkIn(a.getCheckIn()  != null ? a.getCheckIn().format(TIME_FMT)  : null)
                .checkOut(a.getCheckOut() != null ? a.getCheckOut().format(TIME_FMT) : null)
                .workingHours(a.getWorkingHours())
                .attendanceStatus(a.getAttendanceStatus())
                .build();
    }
}
