package com.example.Hospital.DTO.response;

import com.example.Hospital.model.enums.AppointmentStatus;
import com.example.Hospital.model.enums.AppointmentType;
import com.example.Hospital.model.enums.CollectionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {

    private Long id;

    private Long patientId;
    private String patientName;

    private Long doctorId;
    private String doctorName;

    private Long serviceId;
    private String serviceName;

    private AppointmentType type;

    private Instant preferredAt;
    private Instant startAt;
    private Instant endAt;

    private String patientTimeZone;

    private AppointmentStatus status;

    private String reason;

    private CollectionType collectionType;
    private String collectionAddress;

    private String cancellationReason;

    private Instant createdAt;
    private Instant updatedAt;
}
