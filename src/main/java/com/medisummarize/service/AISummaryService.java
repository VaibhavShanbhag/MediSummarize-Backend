package com.medisummarize.service;

import com.medisummarize.DTO.MedicalReportEmailRequestDTO;
import com.medisummarize.model.AISummary;
import com.medisummarize.repository.AISummaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AISummaryService {
    @Autowired
    public AISummaryRepository aiSummaryRepository;
    @Autowired
    public OpenAISummary openAISummary;
    @Autowired
    public SendEmailService sendEmailService;

    public AISummary createAISummary(AISummary aiSummary) {
        return aiSummaryRepository.save(aiSummary);
    }

    public AISummary getAISummaryById(String id) {
        return aiSummaryRepository.findById(id).orElse(null);
    }

    public List<AISummary> getAISummariesByReportId(String reportId) {
        return aiSummaryRepository.findByReportId(reportId);
    }

    public List<AISummary> getAllAISummaries() {
        return aiSummaryRepository.findAll();
    }

    public String generateAISummary(String extractedText) {
        if (extractedText == null || extractedText.isEmpty()) {
            throw new IllegalArgumentException("Extracted text cannot be null or empty.");
        }
        // Call the OpenAI service to generate a summary
        String summary = openAISummary.summarize(extractedText);
        if (summary == null || summary.isEmpty()) {
            throw new RuntimeException("Failed to generate summary from OpenAI.");
        }
        return summary;
    }

    public String sendMedicalReportToPatient(MedicalReportEmailRequestDTO medicalReportEmailRequestDTO) {
        String subject = "Summary of Your Medical Report";
        String body = """
                Dear %s,
                
                Your recent medical report is ready. Below is a brief summary:
                
                📝 Summary:
                %s
                
                📥 Download Report:
                %s
                
                👨‍⚕️ Reviewed By:
                
                Dr. %s
                
                Thank you for using MediSummarize.
                
                Warm regards,
                Team MediSummarize
                """.formatted(
                medicalReportEmailRequestDTO.patientName(),
                medicalReportEmailRequestDTO.summaryText(),
                medicalReportEmailRequestDTO.reportFileLink(),
                medicalReportEmailRequestDTO.doctorName()
        );
        return sendEmailService.sendEmailMedicalReport(medicalReportEmailRequestDTO.patientEmail(), subject, body);
    }
}
