package com.medisummarize.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AISummary {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String summaryId;
    private String reportId;
    @Column(columnDefinition = "TEXT")
    private String summaryText;
    private LocalDateTime createdAt = LocalDateTime.now();
}