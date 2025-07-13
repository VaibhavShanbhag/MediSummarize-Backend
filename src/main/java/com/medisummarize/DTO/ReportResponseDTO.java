package com.medisummarize.DTO;

import com.medisummarize.model.Report;

public record ReportResponseDTO(Report report, String status) {
}
