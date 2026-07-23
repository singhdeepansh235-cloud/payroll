package com.srmcem.payroll.controller;

import com.srmcem.payroll.dto.DocumentResponse;
import com.srmcem.payroll.enums.DocumentType;
import com.srmcem.payroll.response.ApiResponse;
import com.srmcem.payroll.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST API endpoints for Employee Document Management.
 */
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DocumentResponse>> uploadDocument(
            @RequestParam("employeeId") Long employeeId,
            @RequestParam("documentType") DocumentType documentType,
            @RequestParam("file") MultipartFile file) {

        DocumentResponse response = documentService.uploadDocument(employeeId, documentType, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document uploaded successfully.", response));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getEmployeeDocuments(
            @PathVariable Long employeeId) {

        List<DocumentResponse> documents = documentService.getDocumentsByEmployee(employeeId);
        return ResponseEntity.ok(ApiResponse.success("Employee documents fetched successfully.", documents));
    }

    @GetMapping("/download/{documentId}")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long documentId) {
        byte[] data = documentService.downloadDocument(documentId);
        String mimeType = documentService.getMimeType(documentId);
        String fileName = documentService.getFileName(documentId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType(mimeType))
                .body(data);
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(@PathVariable Long documentId) {
        documentService.deleteDocument(documentId);
        return ResponseEntity.ok(ApiResponse.success("Document deleted successfully.", null));
    }
}
