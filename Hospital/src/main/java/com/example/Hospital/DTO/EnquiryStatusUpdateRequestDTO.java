package com.example.Hospital.dto;

import com.example.Hospital.model.enums.EnquiryStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnquiryStatusUpdateRequestDTO {

    @NotNull(message = "Status is required")
    private EnquiryStatus status;

    @Size(max = 5000, message = "Staff response must be at most 5000 characters")
    private String staffResponse;
}

