package com.srmcem.payroll.service;

import com.srmcem.payroll.dto.DashboardStats;

public interface DashboardService {

    /**
     * Aggregates all dashboard metrics into a single {@link DashboardStats} snapshot.
     *
     * <p>Any metric whose underlying table does not yet exist (e.g., before a module
     * is built) is returned as {@code 0} — the dashboard never throws.
     */
    DashboardStats getStats();
}
