package com.srmcem.payroll.controller;

import com.srmcem.payroll.dto.AttendanceRequest;
import com.srmcem.payroll.dto.AttendanceResponse;
import com.srmcem.payroll.dto.MonthlyAttendanceResponse;
import com.srmcem.payroll.response.ApiResponse;
import com.srmcem.payroll.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for the Attendance module.
 *
 * <p>Base path: {@code /api/attendance}
 *
 * <table border="1" cellpadding="4">
 *   <tr><th>Method</th><th>Path</th><th>Description</th></tr>
 *   <tr><td>POST</td><td>/api/attendance</td>
 *       <td>Mark attendance for an employee</td></tr>
 *   <tr><td>PUT</td> <td>/api/attendance/{id}</td>
 *       <td>Update an attendance record</td></tr>
 *   <tr><td>GET</td> <td>/api/attendance/{id}</td>
 *       <td>View a single attendance record</td></tr>
 *   <tr><td>GET</td> <td>/api/attendance/employee/{employeeId}</td>
 *       <td>Full attendance history for an employee</td></tr>
 *   <tr><td>GET</td> <td>/api/attendance/date?date=yyyy-MM-dd</td>
 *       <td>Daily roll-call for all employees on a given date</td></tr>
 *   <tr><td>GET</td> <td>/api/attendance/monthly/{employeeId}?period=MMMM-yyyy</td>
 *       <td>Monthly attendance report for an employee</td></tr>
 * </table>
 *
 * <p>All endpoints require authentication (enforced by {@code SecurityConfig}).
 */
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    // -----------------------------------------------------------------------
    // POST /api/attendance — Mark Attendance
    // -----------------------------------------------------------------------

    @PostMapping
    public ResponseEntity<ApiResponse<AttendanceResponse>> markAttendance(
            @Valid @RequestBody AttendanceRequest request) {

        AttendanceResponse response = attendanceService.markAttendance(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Attendance marked successfully.", response));
    }

    // -----------------------------------------------------------------------
    // PUT /api/attendance/{id} — Update Attendance
    // -----------------------------------------------------------------------

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendanceResponse>> updateAttendance(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceRequest request) {

        AttendanceResponse response = attendanceService.updateAttendance(id, request);
        return ResponseEntity.ok(
                ApiResponse.success("Attendance updated successfully.", response));
    }

    // -----------------------------------------------------------------------
    // GET /api/attendance/{id} — View Single Record
    // -----------------------------------------------------------------------

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendanceResponse>> getAttendanceById(
            @PathVariable Long id) {

        AttendanceResponse response = attendanceService.getAttendanceById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Attendance record fetched successfully.", response));
    }

    // -----------------------------------------------------------------------
    // GET /api/attendance/employee/{employeeId} — History by Employee
    // -----------------------------------------------------------------------

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getAttendanceByEmployee(
            @PathVariable Long employeeId) {

        List<AttendanceResponse> records = attendanceService.getAttendanceByEmployee(employeeId);
        return ResponseEntity.ok(
                ApiResponse.success("Attendance records fetched successfully.", records));
    }

    // -----------------------------------------------------------------------
    // GET /api/attendance/date?date=yyyy-MM-dd — Daily Roll-call
    // -----------------------------------------------------------------------

    /**
     * Returns all employee attendance records for the given date.
     * Defaults to today when {@code date} is not provided.
     *
     * @param date optional ISO date string {@code "yyyy-MM-dd"}
     */
    @GetMapping("/date")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getAttendanceByDate(
            @RequestParam(required = false) String date) {

        List<AttendanceResponse> records = attendanceService.getAttendanceByDate(date);
        return ResponseEntity.ok(
                ApiResponse.success("Attendance for date fetched successfully.", records));
    }

    // -----------------------------------------------------------------------
    // GET /api/attendance/monthly/{employeeId}?period=MMMM-yyyy
    // -----------------------------------------------------------------------

    /**
     * Returns a monthly attendance report for the given employee.
     *
     * @param employeeId employee ID
     * @param period     month in {@code "MMMM-yyyy"} format, e.g. {@code "July-2026"}
     */
    @GetMapping("/monthly/{employeeId}")
    public ResponseEntity<ApiResponse<MonthlyAttendanceResponse>> getMonthlyAttendance(
            @PathVariable Long employeeId,
            @RequestParam String period) {

        MonthlyAttendanceResponse report =
                attendanceService.getMonthlyAttendance(employeeId, period);
        return ResponseEntity.ok(
                ApiResponse.success("Monthly attendance report generated.", report));
    }
}
