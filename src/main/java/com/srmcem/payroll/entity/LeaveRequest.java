package com.srmcem.payroll.entity;

import com.srmcem.payroll.enums.LeaveStatus;
import com.srmcem.payroll.enums.LeaveType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Represents a leave request submitted by an employee.
 *
 * <p>Maps to the {@code leave_requests} table referenced by
 * {@code DashboardServiceImpl} which queries:
 * <pre>
 *   WHERE status = 'APPROVED'
 *     AND start_date <= today
 *     AND end_date   >= today
 * </pre>
 *
 * <p>Column names {@code start_date}, {@code end_date}, and {@code status}
 * must match exactly the dashboard SQL.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "leave_requests")
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long leaveId;

    /** The employee who submitted this leave request. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false, length = 20)
    private LeaveType leaveType;

    /** Inclusive start date. Column name matches dashboard SQL. */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /** Inclusive end date. Column name matches dashboard SQL. */
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /** Number of days requested — computed at apply time. */
    @Column(name = "total_days", nullable = false)
    private int totalDays;

    @Column(length = 500)
    private String reason;

    /**
     * Lifecycle status — stored as the enum name.
     * Column name {@code status} matches the dashboard SQL.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private LeaveStatus status = LeaveStatus.PENDING;

    /** Optional remarks added by the admin when approving or rejecting. */
    @Column(name = "admin_remarks", length = 500)
    private String adminRemarks;

    /** Date the leave was applied. */
    @Column(name = "applied_on", nullable = false)
    @Builder.Default
    private LocalDate appliedOn = LocalDate.now();
}
