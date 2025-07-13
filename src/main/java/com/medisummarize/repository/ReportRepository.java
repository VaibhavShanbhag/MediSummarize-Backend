package com.medisummarize.repository;

import com.medisummarize.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, String> {
     List<Report> findByPatientId(Long patientId);
     List<Report> findByDoctorId(Long doctorId);
}
