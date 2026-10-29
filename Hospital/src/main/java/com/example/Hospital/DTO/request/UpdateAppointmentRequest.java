package com.example.Hospital.DTO.request;

import com.example.Hospital.model.enums.AppointmentType;
import com.example.Hospital.model.enums.CollectionType;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class UpdateAppointmentRequest {

    private Instant preferredAt;

    private AppointmentType type;

    private Long serviceId;

    @Size(max = 5000, message = "Reason cannot exceed 5000 characters")
    private String reason;

    @Size(max = 60, message = "Time zone cannot exceed 60 characters")
    private String patientTimeZone;

    private CollectionType collectionType;

    private String collectionAddress;
}
