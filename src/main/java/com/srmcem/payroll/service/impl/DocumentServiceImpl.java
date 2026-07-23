package com.srmcem.payroll.service.impl;

import com.srmcem.payroll.dto.DocumentResponse;
import com.srmcem.payroll.entity.Employee;
import com.srmcem.payroll.entity.EmployeeDocument;
import com.srmcem.payroll.enums.DocumentType;
import com.srmcem.payroll.exception.BadRequestException;
import com.srmcem.payroll.exception.ResourceNotFoundException;
import com.srmcem.payroll.repository.EmployeeDocumentRepository;
import com.srmcem.payroll.repository.EmployeeRepository;
import com.srmcem.payroll.service.DocumentService;
import com.srmcem.payroll.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final EmployeeDocumentRepository documentRepository;
    private final EmployeeRepository employeeRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    private static final List<String> ALLOWED_EXTENSIONS = List.of(".pdf", ".jpg", ".jpeg", ".png");

    @Override
    @Transactional
    public DocumentResponse uploadDocument(Long employeeId, DocumentType documentType, MultipartFile file) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        if (file.isEmpty()) {
            throw new BadRequestException("Failed to store empty file.");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown");
        validateFileType(originalFilename);

        try {
            // Ensure directory exists
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            // Generate unique filename to prevent overwriting
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFilename = UUID.randomUUID().toString() + extension;
            Path targetLocation = uploadPath.resolve(uniqueFilename);

            // Copy file to target location
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            EmployeeDocument document = EmployeeDocument.builder()
                    .employee(employee)
                    .documentType(documentType)
                    .fileName(originalFilename)
                    .filePath(targetLocation.toString())
                    .uploadDate(LocalDate.now())
                    .build();

            EmployeeDocument saved = documentRepository.save(document);
            log.info("Document uploaded successfully: {} for employeeId: {}", originalFilename, employeeId);
            return toResponse(saved);

        } catch (IOException ex) {
            log.error("Could not store file {}. Please try again!", originalFilename, ex);
            throw new RuntimeException("Could not store file " + originalFilename + ". Please try again!", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByEmployee(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee", "id", employeeId);
        }
        return documentRepository.findByEmployee_EmployeeIdOrderByUploadDateDesc(employeeId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadDocument(Long documentId) {
        EmployeeDocument document = findDocumentOrThrow(documentId);
        try {
            Path path = Paths.get(document.getFilePath()).normalize();
            return Files.readAllBytes(path);
        } catch (IOException e) {
            log.error("Error reading file: {}", document.getFilePath(), e);
            throw new RuntimeException("Error reading file.", e);
        }
    }

    @Override
    @Transactional
    public void deleteDocument(Long documentId) {
        EmployeeDocument document = findDocumentOrThrow(documentId);
        
        try {
            Path path = Paths.get(document.getFilePath()).normalize();
            Files.deleteIfExists(path);
            documentRepository.delete(document);
            log.info("Deleted document id: {}", documentId);
        } catch (IOException e) {
            log.error("Error deleting file: {}", document.getFilePath(), e);
            throw new RuntimeException("Error deleting physical file.", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String getMimeType(Long documentId) {
        EmployeeDocument document = findDocumentOrThrow(documentId);
        String name = document.getFileName().toLowerCase();
        if (name.endsWith(".pdf")) return "application/pdf";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".png")) return "image/png";
        return "application/octet-stream";
    }

    @Override
    @Transactional(readOnly = true)
    public String getFileName(Long documentId) {
        return findDocumentOrThrow(documentId).getFileName();
    }

    // --- Helpers ---

    private EmployeeDocument findDocumentOrThrow(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "documentId", id));
    }

    private void validateFileType(String filename) {
        if (!filename.contains(".")) {
            throw new BadRequestException("File must have an extension.");
        }
        String ext = filename.substring(filename.lastIndexOf(".")).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BadRequestException("Invalid file type. Only PDF, JPG, JPEG, and PNG are allowed.");
        }
    }

    private DocumentResponse toResponse(EmployeeDocument doc) {
        return DocumentResponse.builder()
                .documentId(doc.getDocumentId())
                .employeeId(doc.getEmployee().getEmployeeId())
                .employeeName(doc.getEmployee().getFirstName() + " " + doc.getEmployee().getLastName())
                .documentType(doc.getDocumentType())
                .fileName(doc.getFileName())
                .uploadDate(DateUtil.format(doc.getUploadDate()))
                .build();
    }
}
