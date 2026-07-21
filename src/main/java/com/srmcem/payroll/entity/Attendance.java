package com.srmcem.payroll.entity;

import com.srmcem.payroll.enums.AttendanceStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Records an employee's daily attendance.
 *
 * <p>Maps to the {@code attendance} table referenced by {@code DashboardServiceImpl}
 * which queries: {@code WHERE attendance_date = ? AND status = 'PRESENT'}.
 *
 * <p>A unique constraint on {@code (employee_id, attendance_date)} ensures only
 * one record per employee per day.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "attendance",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_attendance_employee_date",
        columnNames = {"employee_id", "attendance_date"}
    )
)
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attendanceId;

    /** The employee this attendance record belongs to. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /**
     * The calendar date of attendance.
     * Column name matches the dashboard SQL: {@code attendance_date}.
     */
    @Column(name = "attendance_date", nullable = false)
    private LocalDate date;

    /** Time the employee checked in. Null when status is ABSENT/ON_LEAVE/HOLIDAY. */
    @Column(name = "check_in")
    private LocalTime checkIn;

    /** Time the employee checked out. Null when status is ABSENT/ON_LEAVE/HOLIDAY. */
    @Column(name = "check_out")
    private LocalTime checkOut;

    /**
     * Total working hours for the day, computed at mark/update time.
     * Stored as a decimal (e.g., 8.50 = 8 h 30 min).
     */
    @Column(name = "working_hours", precision = 4, scale = 2)
    private Double workingHours;

    /**
     * Attendance status — stored as the enum name.
     * Column name {@code status} matches the dashboard SQL.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AttendanceStatus attendanceStatus;
}
