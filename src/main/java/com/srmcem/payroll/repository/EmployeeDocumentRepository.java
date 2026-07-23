package com.srmcem.payroll.repository;

import com.srmcem.payroll.entity.EmployeeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeDocumentRepository extends JpaRepository<EmployeeDocument, Long> {
    
    List<EmployeeDocument> findByEmployee_EmployeeIdOrderByUploadDateDesc(Long employeeId);
}
