package com.medisummarize.repository;

import com.medisummarize.model.AISummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AISummaryRepository extends JpaRepository<AISummary, String> {
    List<AISummary> findByReportId(String reportId);
}
