package com.example.Hospital.service;

import com.example.Hospital.dto.EnquiryRequestDTO;
import com.example.Hospital.dto.EnquiryResponseDTO;
import com.example.Hospital.dto.EnquiryStatusUpdateRequestDTO;
import com.example.Hospital.model.enums.EnquiryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EnquiryService {

    EnquiryResponseDTO create(EnquiryRequestDTO request);

    EnquiryResponseDTO getById(Long id);

    Page<EnquiryResponseDTO> getAll(EnquiryStatus status, Long patientId, Pageable pageable);

    EnquiryResponseDTO updateStatus(Long id, EnquiryStatusUpdateRequestDTO request);

    void delete(Long id);
}

