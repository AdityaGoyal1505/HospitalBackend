package com.example.Hospital.DTO.request;

import com.example.Hospital.model.enums.AppointmentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAppointmentStatusRequest {

    @NotNull(message = "Appointment status is required")
    private AppointmentStatus status;

    @Size(max = 5000, message = "Cancellation reason cannot exceed 5000 characters")
    private String cancellationReason;
}
