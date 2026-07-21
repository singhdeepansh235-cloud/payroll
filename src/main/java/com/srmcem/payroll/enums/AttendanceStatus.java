package com.srmcem.payroll.enums;

/**
 * Attendance status values for a given employee on a given day.
 *
 * <p>The dashboard query checks: {@code WHERE status = 'PRESENT'}.
 */
public enum AttendanceStatus {
    PRESENT,
    ABSENT,
    HALF_DAY,
    ON_LEAVE,
    HOLIDAY
}
