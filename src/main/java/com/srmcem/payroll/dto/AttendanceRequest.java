package com.srmcem.payroll.dto;

import com.srmcem.payroll.enums.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request body for marking or updating an attendance record.
 *
 * <p>Times are accepted as ISO-8601 strings ({@code "HH:mm"} or {@code "HH:mm:ss"})
 * and parsed in the service layer.
 *
 * <p>If {@code attendanceStatus} is {@code ABSENT}, {@code ON_LEAVE}, or
 * {@code HOLIDAY}, {@code checkIn} and {@code checkOut} may be omitted.
 */
@Data
public class AttendanceRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    /**
     * Attendance date as ISO string {@code "yyyy-MM-dd"}.
     * Defaults to today in the service when null.
     */
    private String date;

    /**
     * Check-in time as {@code "HH:mm"} or {@code "HH:mm:ss"}.
     * Optional for ABSENT / ON_LEAVE / HOLIDAY.
     */
    private String checkIn;

    /**
     * Check-out time as {@code "HH:mm"} or {@code "HH:mm:ss"}.
     * Optional for ABSENT / ON_LEAVE / HOLIDAY.
     */
    private String checkOut;

    @NotNull(message = "Attendance status is required")
    private AttendanceStatus attendanceStatus;
}
