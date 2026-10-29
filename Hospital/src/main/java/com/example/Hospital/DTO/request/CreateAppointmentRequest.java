
package com.example.Hospital.DTO.request;

import com.example.Hospital.model.enums.AppointmentType;
import com.example.Hospital.model.enums.CollectionType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class CreateAppointmentRequest {

    @NotNull(message = "Doctor must be selected")
    @Positive(message = "Doctor ID must be positive")
    private Long doctorId;

    @NotNull(message = "Preferred appointment time is required")
    @Future(message = "Preferred appointment time must be in the future")
    private Instant preferredAt;

    @Size(max = 5000, message = "Reason cannot exceed 5000 characters")
    private String reason;

    @NotNull(message = "Appointment type is required")
    private AppointmentType type;

    private Long serviceId;

    @Size(max = 60, message = "Time zone cannot exceed 60 characters")
    private String patientTimeZone;

    private CollectionType collectionType;

    @Size(max = 2000, message = "Collection address cannot exceed 2000 characters")
    private String collectionAddress;
}
