package com.srmcem.payroll.service.impl;

import com.srmcem.payroll.dto.AuditLogDto;
import com.srmcem.payroll.entity.AuditLog;
import com.srmcem.payroll.repository.AuditLogRepository;
import com.srmcem.payroll.service.AuditLogService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public void log(String action, String module) {
        String username = getCurrentUsername();
        log(username, action, module);
    }

    @Override
    @Transactional
    public void log(String username, String action, String module) {
        AuditLog auditLog = AuditLog.builder()
                .username(username)
                .action(action)
                .module(module)
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(auditLog);
        log.debug("Audit Log saved: User='{}', Action='{}', Module='{}'", username, action, module);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDto> getLogs(String search, String module, LocalDate startDate, LocalDate endDate, String sortBy, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir != null ? sortDir : "DESC"), sortBy != null ? sortBy : "timestamp");
        
        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (module != null && !module.trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("module")), module.trim().toLowerCase()));
            }

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), startDate.atStartOfDay()));
            }

            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), endDate.atTime(LocalTime.MAX)));
            }

            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                Predicate searchPred = cb.or(
                        cb.like(cb.lower(root.get("username")), searchPattern),
                        cb.like(cb.lower(root.get("action")), searchPattern)
                );
                predicates.add(searchPred);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return auditLogRepository.findAll(spec, sort).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "SYSTEM";
    }

    private AuditLogDto toDto(AuditLog log) {
        return AuditLogDto.builder()
                .id(log.getId())
                .username(log.getUsername())
                .action(log.getAction())
                .module(log.getModule())
                .timestamp(log.getTimestamp())
                .build();
    }
}
