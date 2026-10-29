
package com.example.Hospital.DTO.response;

import com.example.Hospital.model.DoctorSchedule;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public record DoctorScheduleResponse(
        Long id,
        Long doctorId,
        String doctorName,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        LocalDate validFrom,
        LocalDate validUntil,
        Set<LocalDate> blockedDates,
        Set<LocalDate> leaveDates,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {

    public DoctorScheduleResponse {
        blockedDates = blockedDates == null ? Set.of() : Set.copyOf(blockedDates);

        leaveDates = leaveDates == null ? Set.of() : Set.copyOf(leaveDates);
    }

    public static DoctorScheduleResponse from(DoctorSchedule schedule) {
        return new DoctorScheduleResponse(
                schedule.getId(),
                schedule.getDoctor().getId(),
                schedule.getDoctor().getName(),
                schedule.getDayOfWeek(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getValidFrom(),
                schedule.getValidUntil(),
                schedule.getBlockedDates(),
                schedule.getLeaveDates(),
                schedule.isActive(),
                schedule.getCreatedAt(),
                schedule.getUpdatedAt()
        );
    }
}
