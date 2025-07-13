package com.medisummarize.DTO;

public record ReportRequestDTO(String fileURL, String extractedText, String patientEmail, String doctorEmail) {
}
