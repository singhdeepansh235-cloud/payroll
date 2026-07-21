package com.srmcem.payroll.enums;

/**
 * Lifecycle status of a leave request.
 *
 * <p>The dashboard query checks: {@code WHERE status = 'APPROVED'}.
 */
public enum LeaveStatus {
    PENDING,
    APPROVED,
    REJECTED
}
