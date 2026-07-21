package com.srmcem.payroll.dto;

import com.srmcem.payroll.enums.AttendanceStatus;
import lombok.Builder;
import lombok.Data;

/**
 * Read-only projection of an Attendance record returned to the client.
 *
 * <p>Employee details are flattened (id + full name) to avoid extra round-trips.
 * All times are formatted as {@code "HH:mm"} strings.
 */
@Data
@Builder
public class AttendanceResponse {

    private Long   attendanceId;
    private Long   employeeId;
    private String employeeName;      // firstName + lastName

    private String date;              // formatted dd-MM-yyyy
    private String checkIn;           // formatted HH:mm
    private String checkOut;          // formatted HH:mm
    private Double workingHours;
    private AttendanceStatus attendanceStatus;
}
