package com.srmcem.payroll.controller;

import com.srmcem.payroll.dto.AuditLogDto;
import com.srmcem.payroll.response.ApiResponse;
import com.srmcem.payroll.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AuditLogDto>>> getLogs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
            
        List<AuditLogDto> logs = auditLogService.getLogs(search, module, startDate, endDate, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved successfully.", logs));
    }
}
