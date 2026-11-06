package com.example.Hospital.repository;

import com.example.Hospital.model.PatientDocument;
import com.example.Hospital.model.enums.DocumentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientDocumentRepository extends JpaRepository<PatientDocument, Long> {

    Page<PatientDocument> findByPatientId(Long patientId, Pageable pageable);

    Page<PatientDocument> findByPatientIdAndDocumentType(
            Long patientId, DocumentType documentType, Pageable pageable);
}
