package com.srmcem.payroll.service;

import com.srmcem.payroll.dto.DocumentResponse;
import com.srmcem.payroll.enums.DocumentType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {
    
    DocumentResponse uploadDocument(Long employeeId, DocumentType documentType, MultipartFile file);
    
    List<DocumentResponse> getDocumentsByEmployee(Long employeeId);
    
    byte[] downloadDocument(Long documentId);
    
    void deleteDocument(Long documentId);
    
    String getMimeType(Long documentId);
    
    String getFileName(Long documentId);
}
