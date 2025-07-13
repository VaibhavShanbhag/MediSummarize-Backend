package com.medisummarize.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@Data
@NoArgsConstructor
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "reportId")
    private String id;
    private String fileUrl;
    @Column(columnDefinition = "TEXT")
    private String extractedText;
    private Long doctorId;
    private Long patientId;
    private String reportType;
    private LocalDateTime uploadedAt = LocalDateTime.now();
}
