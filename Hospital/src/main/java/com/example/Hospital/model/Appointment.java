package com.example.Hospital.model;

import com.example.Hospital.model.enums.AppointmentStatus;
import com.example.Hospital.model.enums.AppointmentType;
import com.example.Hospital.model.enums.CollectionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "appointments",
        indexes = {
                @Index(name = "idx_appointment_patient",
                        columnList = "patient_id"),
                @Index(name = "idx_appointment_doctor_start",
                        columnList = "doctor_id, start_at"),
                @Index(name = "idx_appointment_status",
                        columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    // Can remain null until staff assigns a doctor.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private User doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private HospitalService service;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentType type;

    // Requested time; can differ from the final confirmed slot.
    private Instant preferredAt;

    // Final appointment time, stored as an absolute timestamp.
    private Instant startAt;

    private Instant endAt;

    // Example: Asia/Kolkata. Used to interpret/display the user's times.
    @Column(length = 60)
    private String patientTimeZone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentStatus status = AppointmentStatus.CONFIRMED;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private CollectionType collectionType;

    @Column(columnDefinition = "TEXT")
    private String collectionAddress;

    @Column(columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();

        createdAt = now;
        updatedAt = now;

        if (status == null) {
            status = AppointmentStatus.CONFIRMED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}