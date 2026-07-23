package com.srmcem.payroll.report.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class ReportUtil {

    private ReportUtil() {}

    public static HttpHeaders createAttachmentHeaders(String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
        return headers;
    }

    public static MediaType getMediaTypeForFormat(String format) {
        return switch (format.toLowerCase()) {
            case "pdf" -> MediaType.APPLICATION_PDF;
            case "excel", "xlsx" -> MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case "csv" -> MediaType.valueOf("text/csv");
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }
}
