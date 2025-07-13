package com.medisummarize.DTO;

public record ReportRequestDTO(String fileURL, String extractedText, String reportType, String patientEmail, String doctorEmail) {
}
