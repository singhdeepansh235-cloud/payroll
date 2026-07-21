package com.srmcem.payroll.repository;

import com.srmcem.payroll.entity.Attendance;
import com.srmcem.payroll.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Attendance}.
 */
@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    /** Fetches attendance for a specific employee on a specific date. */
    Optional<Attendance> findByEmployee_EmployeeIdAndDate(Long employeeId, LocalDate date);

    /** All attendance records for a given employee, ordered by date descending. */
    List<Attendance> findByEmployee_EmployeeIdOrderByDateDesc(Long employeeId);

    /** All attendance records for a given employee within a date range (monthly report). */
    List<Attendance> findByEmployee_EmployeeIdAndDateBetweenOrderByDateAsc(
            Long employeeId, LocalDate startDate, LocalDate endDate);

    /** All records on a specific date (useful for daily roll-call). */
    List<Attendance> findByDateOrderByEmployee_FirstNameAsc(LocalDate date);

    /**
     * Monthly attendance summary counts for a single employee.
     * Returns counts grouped by status.
     */
    @Query("""
            SELECT a.attendanceStatus, COUNT(a)
            FROM Attendance a
            WHERE a.employee.employeeId = :employeeId
              AND a.date BETWEEN :startDate AND :endDate
            GROUP BY a.attendanceStatus
            """)
    List<Object[]> countByStatusForEmployee(
            @Param("employeeId") Long employeeId,
            @Param("startDate")  LocalDate startDate,
            @Param("endDate")    LocalDate endDate);

    /** Checks whether an attendance record already exists for the given employee+date. */
    boolean existsByEmployee_EmployeeIdAndDate(Long employeeId, LocalDate date);

    /** All records for a given employee with a specific status. */
    List<Attendance> findByEmployee_EmployeeIdAndAttendanceStatus(
            Long employeeId, AttendanceStatus status);
}
