package com.example.Hospital.Service;

import com.example.Hospital.DTO.FaqRequestDTO;
import com.example.Hospital.DTO.FaqResponseDTO;
import com.example.Hospital.DTO.FaqUpdateRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FaqService {

    FaqResponseDTO create(FaqRequestDTO request);

    FaqResponseDTO getById(Long faqId);

    Page<FaqResponseDTO> getAllActive(String category, Pageable pageable);

    Page<FaqResponseDTO> getAllForAdmin(Pageable pageable);

    FaqResponseDTO update(Long faqId, FaqUpdateRequestDTO request);

    void deactivate(Long faqId);

    void activate(Long faqId);

    void delete(Long faqId);
}
