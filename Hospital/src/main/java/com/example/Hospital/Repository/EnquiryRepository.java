package com.example.Hospital.repository;

import com.example.Hospital.model.Enquiry;
import com.example.Hospital.model.enums.EnquiryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnquiryRepository extends JpaRepository<Enquiry, Long> {

    Page<Enquiry> findByStatus(EnquiryStatus status, Pageable pageable);

    Page<Enquiry> findByPatientId(Long patientId, Pageable pageable);

    Page<Enquiry> findByPatientIdAndStatus(Long patientId, EnquiryStatus status, Pageable pageable);
}

