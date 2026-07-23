package com.srmcem.payroll.repository;

import com.srmcem.payroll.entity.CompanySettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanySettingsRepository extends JpaRepository<CompanySettings, Long> {
}
