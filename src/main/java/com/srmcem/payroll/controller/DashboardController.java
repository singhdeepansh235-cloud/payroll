package com.srmcem.payroll.controller;

import com.srmcem.payroll.dto.DashboardStats;
import com.srmcem.payroll.response.ApiResponse;
import com.srmcem.payroll.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoint for the Admin Dashboard.
 *
 * <p>Base path: {@code /api/dashboard}
 *
 * <ul>
 *   <li>{@code GET /api/dashboard/stats} — returns all five dashboard metrics</li>
 * </ul>
 *
 * <p>This endpoint is protected — the caller must be authenticated
 * (enforced by {@code SecurityConfig}).
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Returns a snapshot of key payroll system metrics for today.
     *
     * <p>Sample response:
     * <pre>
     * {
     *   "success": true,
     *   "message": "Dashboard stats fetched successfully.",
     *   "data": {
     *     "totalEmployees": 42,
     *     "totalDepartments": 5,
     *     "presentToday": 38,
     *     "onLeaveToday": 2,
     *     "totalPayrollCurrentMonth": 950000.00
     *   },
     *   "timestamp": "2026-07-19T11:45:00"
     * }
     * </pre>
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStats>> getStats() {
        DashboardStats stats = dashboardService.getStats();
        return ResponseEntity.ok(
                ApiResponse.success("Dashboard stats fetched successfully.", stats));
    }
}
