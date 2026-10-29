
package com.example.Hospital.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "doctor_schedules",
        indexes = {
                @Index(
                        name = "idx_schedule_doctor_day",
                        columnList = "doctor_id, day_of_week"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class DoctorSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 15)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    private LocalDate validFrom;

    private LocalDate validUntil;

    @ElementCollection
    @CollectionTable(
            name = "doctor_schedule_blocked_dates",
            joinColumns = @JoinColumn(name = "schedule_id")
    )
    @Column(name = "blocked_date", nullable = false)
    private Set<LocalDate> blockedDates = new HashSet<>();

    @ElementCollection
    @CollectionTable(
            name = "doctor_schedule_leave_dates",
            joinColumns = @JoinColumn(name = "schedule_id")
    )
    @Column(name = "leave_date", nullable = false)
    private Set<LocalDate> leaveDates = new HashSet<>();

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    @PreRemove
    protected void onRemove() {
        // No custom action required.
    }
}
