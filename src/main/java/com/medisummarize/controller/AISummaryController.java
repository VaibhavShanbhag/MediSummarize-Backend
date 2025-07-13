package com.medisummarize.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.medisummarize.DTO.AISummaryResponseDTO;
import com.medisummarize.DTO.MedicalReportEmailRequestDTO;
import com.medisummarize.model.AISummary;
import com.medisummarize.model.Report;
import com.medisummarize.service.AISummaryService;
import com.medisummarize.service.ReportService;
import com.medisummarize.service.SendEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/summary")
public class AISummaryController {
    @Autowired
    public AISummaryService aiSummaryService;
    @Autowired
    public ReportService reportService;

    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping("/generate")
    public ResponseEntity<AISummaryResponseDTO> generateSummary(@RequestParam("reportId") String reportId) {
        try {
            Report report = reportService.getReportById(reportId);
            if (report == null) {
                return ResponseEntity.badRequest().body(new AISummaryResponseDTO(null, "Report not found"));
            }
            List<AISummary> aiSummaries = aiSummaryService.getAISummariesByReportId(reportId);
            if(!aiSummaries.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK).body(new AISummaryResponseDTO(aiSummaries.get(0), "Summary already exists"));
            }

            String summary = aiSummaryService.generateAISummary(report.getExtractedText());
            if (summary == null || summary.isEmpty()) {
                return ResponseEntity.badRequest().body(new AISummaryResponseDTO(null, "Failed to generate summary"));
            }
            AISummary aiSummary = new AISummary();
            aiSummary.setSummaryText(summary);
            aiSummary.setReportId(reportId);
            AISummary createdSummary = aiSummaryService.createAISummary(aiSummary);
            return ResponseEntity.status(HttpStatus.OK).body(new AISummaryResponseDTO(createdSummary, "Summary generated successfully"));
        } catch (Exception e) {
            System.out.println(e.getMessage());

            return ResponseEntity.badRequest().body(null);
        }
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/get/{reportId}")
    public ResponseEntity<Map<String, Object>> getAISummary(@PathVariable String reportId) {
        try {
            Optional<AISummary> aiSummaryOptional = aiSummaryService.getAISummariesByReportId(reportId).stream().findFirst();
            if (aiSummaryOptional.isPresent()) {
                AISummary aiSummary = aiSummaryOptional.get();
                return ResponseEntity.status(HttpStatus.OK).body(Map.of("summary", aiSummary.getSummaryText()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "No summary found for the given report ID"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/get/all")
    public ResponseEntity<Map<String, Object>> getAllAISummaries(@RequestBody JsonNode reports) {
        try {
            var summaries = aiSummaryService.getAllAISummaries();
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            Map<String, Object> result = new HashMap<>();
            for(JsonNode report : reports) {
                String reportId = report.get("id").asText();
                for (AISummary summary : summaries) {
                    if (summary.getReportId().equals(reportId)) {
                        ((ObjectNode) report).putPOJO("summary", summary);
                        break;
                    }
                }
                result.put(reportId, mapper.convertValue(report, Map.class));
            }

            return ResponseEntity.status(HttpStatus.OK).body(Map.of("summaries", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/sendEmail")
    public ResponseEntity<String> sendAISummaryEmail(@RequestBody MedicalReportEmailRequestDTO medicalReportEmailRequestDTO) {
        try {
            String emailResponse = aiSummaryService.sendMedicalReportToPatient(medicalReportEmailRequestDTO);
            return ResponseEntity.status(HttpStatus.OK).body(emailResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send email: " + e.getMessage());
        }
    }

}
