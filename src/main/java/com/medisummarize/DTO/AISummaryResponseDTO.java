package com.medisummarize.DTO;

import com.medisummarize.model.AISummary;

public record AISummaryResponseDTO(AISummary aiSummary, String message) {
}
