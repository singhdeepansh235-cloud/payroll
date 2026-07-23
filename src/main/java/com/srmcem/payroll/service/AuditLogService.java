package com.srmcem.payroll.service;

import com.srmcem.payroll.dto.AuditLogDto;
import java.time.LocalDate;
import java.util.List;

public interface AuditLogService {

    void log(String action, String module);

    void log(String username, String action, String module);

    List<AuditLogDto> getLogs(String search, String module, LocalDate startDate, LocalDate endDate, String sortBy, String sortDir);
}
