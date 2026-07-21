package com.srmcem.payroll.service;

import com.srmcem.payroll.dto.AttendanceRequest;
import com.srmcem.payroll.dto.AttendanceResponse;
import com.srmcem.payroll.dto.MonthlyAttendanceResponse;

import java.util.List;

public interface AttendanceService {

    /**
     * Marks attendance for an employee on the given date (defaults to today).
     * Throws {@code BadRequestException} if a record already exists for that
     * employee on that date.
     */
    AttendanceResponse markAttendance(AttendanceRequest request);

    /**
     * Updates an existing attendance record.
     * Throws {@code ResourceNotFoundException} if the record is not found.
     */
    AttendanceResponse updateAttendance(Long attendanceId, AttendanceRequest request);

    /**
     * Fetches a single attendance record by its ID.
     * Throws {@code ResourceNotFoundException} if not found.
     */
    AttendanceResponse getAttendanceById(Long attendanceId);

    /**
     * Returns the full attendance history for an employee, newest first.
     *
     * @param employeeId the employee to query
     */
    List<AttendanceResponse> getAttendanceByEmployee(Long employeeId);

    /**
     * Returns all attendance records for a specific date (daily roll-call).
     *
     * @param date ISO date string {@code "yyyy-MM-dd"}; defaults to today when null
     */
    List<AttendanceResponse> getAttendanceByDate(String date);

    /**
     * Builds a monthly attendance report for a given employee.
     *
     * @param employeeId the employee to report on
     * @param yearMonth  period in {@code "MMMM-yyyy"} format (e.g. "July-2026")
     */
    MonthlyAttendanceResponse getMonthlyAttendance(Long employeeId, String yearMonth);
}
