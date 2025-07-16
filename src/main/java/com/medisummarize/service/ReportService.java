package com.medisummarize.service;

import com.medisummarize.model.Report;
import com.medisummarize.repository.ReportRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ReportService {
    @Autowired
    public ReportRepository reportRepository;
    @Autowired
    public CloudinaryService cloudinaryService;
    @Autowired
    public SendEmailService sendEmailService;

    public Report createReport(Report report) {
        return reportRepository.save(report);
    }

    public Report getReportById(String id) {
        return reportRepository.findById(id).orElse(null);
    }

    public List<Report> getReportsByPatientId(Long patientId) {
        return reportRepository.findByPatientId(patientId);
    }

    public List<Report> getReportsByDoctorId(Long doctorId) {
        return reportRepository.findByDoctorId(doctorId);
    }

    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    public Map<String, String> processReportFile(MultipartFile file) throws IOException {
        if (!Objects.equals(file.getContentType(), "application/pdf")) {
            throw new IllegalArgumentException("Only PDF files are supported.");
        }
        // Save the file to a temporary location
        File tempFile = File.createTempFile("report", ".pdf");
        file.transferTo(tempFile);

        // Extract text from the PDF file
        String extractedText = extractTextFromPdf(tempFile);

        // Upload the file to Cloudinary
        String uploadResult = cloudinaryService.uploadFile(tempFile);

        // Clean up the temporary file
        tempFile.delete();

        return Map.of(
                "extractedText", extractedText,
                "cloudinaryUrl", uploadResult
        );
    }

    private String extractTextFromPdf(File pdfFile) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document).trim();
        }
    }

    public String sendReportToPatient(String patientEmail, String patientName) {
        String subject = "Your Health Report Has Been Uploaded – View It Online";
        String emailBody = String.format("""
            Dear %s,
            
            Your medical report has been successfully uploaded by your doctor.
            
            To view and download your report, please log in to your MediSummarize account:
            
            🔗 http://localhost:8081/login
            
            If you have any questions or concerns, please contact your healthcare provider directly.
            
            Thank you,
            MediSummarize Team
            """, patientName);
        return sendEmailService.sendEmailAfterUpload(patientEmail, subject, emailBody);
    }

}
