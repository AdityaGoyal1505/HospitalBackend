package com.example.Hospital.Service;

import com.example.Hospital.DTO.EnquiryRequestDTO;
import com.example.Hospital.DTO.EnquiryResponseDTO;
import com.example.Hospital.DTO.EnquiryStatusUpdateRequestDTO;
import com.example.Hospital.model.enums.EnquiryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EnquiryService {

    EnquiryResponseDTO create(Long userId, EnquiryRequestDTO request);
    EnquiryResponseDTO getById(Long id);

    Page<EnquiryResponseDTO> getAll(EnquiryStatus status, Long patientId, Pageable pageable);

    EnquiryResponseDTO updateStatus(Long id, EnquiryStatusUpdateRequestDTO request);

    void delete(Long id);
}

