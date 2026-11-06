package com.example.Hospital.dto;

import com.example.Hospital.model.enums.EnquiryStatus;
import com.example.Hospital.model.enums.EnquiryType;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnquiryResponseDTO {
    private Long id;
    private Long patientId;
    private String patientName;
    private String patientEmail;
    private String patientPhone;
    private String country;
    private EnquiryType enquiryType;
    private String message;
    private String treatmentInterest;
    private LocalDate preferredDate;
    private EnquiryStatus status;
    private String staffResponse;
    private Instant respondedAt;
    private Instant createdAt;
    private Instant updatedAt;
}

