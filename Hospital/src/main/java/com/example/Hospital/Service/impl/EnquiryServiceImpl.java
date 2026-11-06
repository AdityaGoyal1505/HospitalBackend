package com.example.Hospital.service.impl;

import static com.example.Hospital.model.enums.EnquiryStatus.*;

import com.example.Hospital.dto.EnquiryRequestDTO;
import com.example.Hospital.dto.EnquiryResponseDTO;
import com.example.Hospital.dto.EnquiryStatusUpdateRequestDTO;
import com.example.Hospital.model.Enquiry;
import com.example.Hospital.model.enums.EnquiryStatus;
import com.example.Hospital.model.enums.EnquiryType;
import com.example.Hospital.exception.ResourceNotFoundException;
import com.example.Hospital.repository.EnquiryRepository;
import com.example.Hospital.service.EnquiryService;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnquiryServiceImpl implements EnquiryService {

    private static final Map<EnquiryStatus, Set<EnquiryStatus>> ALLOWED_TRANSITIONS = Map.of(
            NEW, Set.of(IN_PROGRESS, RESPONDED, CLOSED),
            IN_PROGRESS, Set.of(RESPONDED, CLOSED),
            RESPONDED, Set.of(CLOSED),
            CLOSED, Set.of());

    private final EnquiryRepository enquiryRepository;

    private EnquiryResponseDTO toResponse(Enquiry enquiry) {
        EnquiryResponseDTO dto = new EnquiryResponseDTO();
        dto.setId(enquiry.getId());
        dto.setPatientId(enquiry.getPatientId());
        dto.setPatientName(enquiry.getPatientName());
        dto.setPatientEmail(enquiry.getPatientEmail());
        dto.setPatientPhone(enquiry.getPatientPhone());
        dto.setCountry(enquiry.getCountry());
        dto.setEnquiryType(enquiry.getEnquiryType());
        dto.setMessage(enquiry.getMessage());
        dto.setTreatmentInterest(enquiry.getTreatmentInterest());
        dto.setPreferredDate(enquiry.getPreferredDate());
        dto.setStatus(enquiry.getStatus());
        dto.setStaffResponse(enquiry.getStaffResponse());
        dto.setRespondedAt(enquiry.getRespondedAt());
        dto.setCreatedAt(enquiry.getCreatedAt());
        dto.setUpdatedAt(enquiry.getUpdatedAt());
        return dto;
    }

    private static String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Override
    @Transactional
    public EnquiryResponseDTO create(EnquiryRequestDTO request) {
        if (request.getEnquiryType() == EnquiryType.INTERNATIONAL
                && clean(request.getCountry()) == null) {
            throw new IllegalArgumentException("Country is required for international enquiries");
        }

        Enquiry enquiry = new Enquiry();
        enquiry.setPatientId(request.getPatientId());
        enquiry.setPatientName(request.getPatientName().trim());
        enquiry.setPatientEmail(request.getPatientEmail().trim().toLowerCase(Locale.ROOT));
        enquiry.setPatientPhone(clean(request.getPatientPhone()));
        enquiry.setCountry(clean(request.getCountry()));
        enquiry.setEnquiryType(request.getEnquiryType());
        enquiry.setMessage(request.getMessage().trim());
        enquiry.setTreatmentInterest(clean(request.getTreatmentInterest()));
        enquiry.setPreferredDate(request.getPreferredDate());
        enquiry.setStatus(NEW); // a visitor can never choose the status

        return toResponse(enquiryRepository.save(enquiry));
    }

    @Override
    @Transactional(readOnly = true)
    public EnquiryResponseDTO getById(Long id) {
        Enquiry enquiry = enquiryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry not found with id " + id));
        return toResponse(enquiry);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EnquiryResponseDTO> getAll(EnquiryStatus status, Long patientId, Pageable pageable) {
        Page<Enquiry> result;
        if (patientId != null && status != null) {
            result = enquiryRepository.findByPatientIdAndStatus(patientId, status, pageable);
        } else if (patientId != null) {
            result = enquiryRepository.findByPatientId(patientId, pageable);
        } else if (status != null) {
            result = enquiryRepository.findByStatus(status, pageable);
        } else {
            result = enquiryRepository.findAll(pageable);
        }
        return result.map(this::toResponse);
    }

    @Override
    @Transactional
    public EnquiryResponseDTO updateStatus(Long id, EnquiryStatusUpdateRequestDTO request) {
        Enquiry enquiry = enquiryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry not found with id " + id));

        EnquiryStatus newStatus = request.getStatus();
        Set<EnquiryStatus> allowed = ALLOWED_TRANSITIONS.get(enquiry.getStatus());
        if (allowed == null || !allowed.contains(newStatus)) {
            throw new IllegalArgumentException(
                    "Cannot change enquiry from " + enquiry.getStatus() + " to " + newStatus);
        }

        enquiry.setStatus(newStatus);
        if (request.getStaffResponse() != null && !request.getStaffResponse().isBlank()) {
            enquiry.setStaffResponse(request.getStaffResponse().trim());
            enquiry.setRespondedAt(Instant.now());
        }
        return toResponse(enquiryRepository.save(enquiry));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Enquiry enquiry = enquiryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry not found with id " + id));
        enquiryRepository.delete(enquiry);
    }
}

