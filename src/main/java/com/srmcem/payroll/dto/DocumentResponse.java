package com.srmcem.payroll.dto;

import com.srmcem.payroll.enums.DocumentType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocumentResponse {
    private Long documentId;
    private Long employeeId;
    private String employeeName;
    private DocumentType documentType;
    private String fileName;
    private String uploadDate;
}
