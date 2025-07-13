package com.medisummarize.DTO;

public record MedicalReportEmailRequestDTO(String patientEmail, String patientName, String summaryText, String reportFileLink, String doctorName) {
}
