package com.srmcem.payroll.repository;

import com.srmcem.payroll.entity.LeaveRequest;
import com.srmcem.payroll.enums.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for {@link LeaveRequest}.
 */
@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    /** Full leave history for a given employee, newest first. */
    List<LeaveRequest> findByEmployee_EmployeeIdOrderByAppliedOnDesc(Long employeeId);

    /** All leave requests with a specific status (e.g. PENDING). */
    List<LeaveRequest> findByStatusOrderByAppliedOnDesc(LeaveStatus status);

    /** Leave history for a given employee filtered by status. */
    List<LeaveRequest> findByEmployee_EmployeeIdAndStatusOrderByAppliedOnDesc(
            Long employeeId, LeaveStatus status);

    /**
     * Checks whether an employee already has an APPROVED or PENDING leave
     * that overlaps the requested date range — prevents double-booking.
     */
    @Query("""
            SELECT COUNT(lr) > 0 FROM LeaveRequest lr
            WHERE lr.employee.employeeId = :employeeId
              AND lr.status IN ('PENDING', 'APPROVED')
              AND lr.startDate <= :endDate
              AND lr.endDate   >= :startDate
            """)
    boolean hasOverlappingLeave(
            @Param("employeeId") Long employeeId,
            @Param("startDate")  LocalDate startDate,
            @Param("endDate")    LocalDate endDate);

    /** All leave requests for all employees, newest first. */
    List<LeaveRequest> findAllByOrderByAppliedOnDesc();

    @Query("""
            SELECT lr FROM LeaveRequest lr
            WHERE :search IS NULL OR :search = ''
               OR LOWER(lr.employee.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(lr.employee.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(lr.employee.email) LIKE LOWER(CONCAT('%', :search, '%'))
            """)
    org.springframework.data.domain.Page<LeaveRequest> searchPaginated(@Param("search") String search, org.springframework.data.domain.Pageable pageable);
}
