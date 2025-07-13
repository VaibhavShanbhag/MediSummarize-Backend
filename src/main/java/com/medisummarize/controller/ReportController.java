package com.medisummarize.controller;

import com.medisummarize.DTO.ReportRequestDTO;
import com.medisummarize.DTO.ReportResponseDTO;
import com.medisummarize.model.Report;
import com.medisummarize.model.User;
import com.medisummarize.service.ReportService;
import com.medisummarize.service.SendEmailService;
import com.medisummarize.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.collect;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {
    @Autowired
    public ReportService reportService;
    @Autowired
    public SendEmailService sendEmailService;
    @Autowired
    public UserService userService;

    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadReport(@RequestParam("file") MultipartFile multipartFile) {
        try {
            Map<String, String> response = reportService.processReportFile(multipartFile);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/generate")
    public ResponseEntity<ReportResponseDTO> generateReport(@RequestBody ReportRequestDTO reportRequestDTO) {
        try {
            Long doctorUserId = userService.getUserByEmail(reportRequestDTO.doctorEmail()).getId();
            Long patientUserId = userService.getUserByEmail(reportRequestDTO.patientEmail()).getId();
            Report report = new Report();
            report.setDoctorId(doctorUserId);
            report.setPatientId(patientUserId);
            report.setFileUrl(reportRequestDTO.fileURL());
            report.setExtractedText(reportRequestDTO.extractedText());
            Report createdReport = reportService.createReport(report);
            String emailResponse = reportService.sendReportToPatient(reportRequestDTO.patientEmail(), userService.getUserByEmail(reportRequestDTO.patientEmail()).getName());
            ReportResponseDTO reportResponseDTO = new ReportResponseDTO(createdReport, emailResponse);
            return ResponseEntity.status(HttpStatus.OK).body(reportResponseDTO);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PreAuthorize("hasRole('DOCTOR') or hasRole('PATIENT')")
    @GetMapping("/{id}")
    public ResponseEntity<ReportResponseDTO> getReportById(@PathVariable String id) {
        try {
            Report report = reportService.getReportById(id);
            if (report == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            ReportResponseDTO reportResponseDTO = new ReportResponseDTO(report, "Report retrieved successfully");
            return ResponseEntity.status(HttpStatus.OK).body(reportResponseDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<Map<String, Object>> getReportsByPatientId(@PathVariable Long patientId) {
        try {
            var reports = reportService.getReportsByPatientId(patientId);
            List<Map<String, Object>> reportList = reports.stream().map(report -> {
                User doctor = userService.getUserById(report.getDoctorId());
                User patient = userService.getUserById(report.getPatientId());
                Map<String, String> doctorInfo = Map.of(
                        "id", String.valueOf(doctor.getId()),
                        "name", doctor.getName(),
                        "email", doctor.getEmail()
                );
                Map<String, String> patientInfo = Map.of(
                        "id", String.valueOf(patient.getId()),
                        "name", patient.getName(),
                        "email", patient.getEmail()
                );
                return Map.of(
                        "id", report.getId(),
                        "fileUrl", report.getFileUrl(),
                        "extractedText", report.getExtractedText(),
                        "doctor", doctorInfo,
                        "patient", patientInfo,
                        "uploadedAt", report.getUploadedAt().toString()
                );
            }).toList();
            System.out.println(reportList.size());
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("reports", reportList));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<Map<String, Object>> getReportsByDoctorId(@PathVariable Long doctorId) {
        try {
            var reports = reportService.getReportsByDoctorId(doctorId);
            List<Map<String, Object>> reportList = reports.stream().map(report -> {
                User doctor = userService.getUserById(report.getDoctorId());
                User patient = userService.getUserById(report.getPatientId());
                Map<String, String> doctorInfo = Map.of(
                        "id", String.valueOf(doctor.getId()),
                        "name", doctor.getName(),
                        "email", doctor.getEmail()
                );
                Map<String, String> patientInfo = Map.of(
                        "id", String.valueOf(patient.getId()),
                        "name", patient.getName(),
                        "email", patient.getEmail()
                );
                return Map.of(
                        "id", report.getId(),
                        "fileUrl", report.getFileUrl(),
                        "extractedText", report.getExtractedText(),
                        "doctor", doctorInfo,
                        "patient", patientInfo,
                        "uploadedAt", report.getUploadedAt().toString()
                );
            }).toList();

            return ResponseEntity.status(HttpStatus.OK).body(Map.of("reports", reportList));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

}
