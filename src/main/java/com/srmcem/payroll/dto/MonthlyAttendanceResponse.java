package com.srmcem.payroll.dto;

import com.srmcem.payroll.enums.AttendanceStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Monthly attendance report for a single employee.
 *
 * <p>Contains:
 * <ul>
 *   <li>The employee ID and name</li>
 *   <li>The year-month period (e.g. "July-2026")</li>
 *   <li>A breakdown of counts per {@link AttendanceStatus}</li>
 *   <li>The day-by-day attendance list for the period</li>
 * </ul>
 */
@Data
@Builder
public class MonthlyAttendanceResponse {

    private Long   employeeId;
    private String employeeName;
    private String period;                       // e.g. "July-2026"

    /** Count of days per status — key is the status name, value is the count. */
    private Map<String, Long> statusSummary;

    /** Day-by-day attendance records for the month. */
    private List<AttendanceResponse> records;
}
