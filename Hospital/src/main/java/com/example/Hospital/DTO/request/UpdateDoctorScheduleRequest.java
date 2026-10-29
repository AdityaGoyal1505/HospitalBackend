package com.example.Hospital.DTO.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public record UpdateDoctorScheduleRequest(

        @NotNull(message = "Day of week is required")
        DayOfWeek dayOfWeek,

        @NotNull(message = "Start time is required")
        LocalTime startTime,

        @NotNull(message = "End time is required")
        LocalTime endTime,

        LocalDate validFrom,

        LocalDate validUntil,

        Set<LocalDate> blockedDates,

        Set<LocalDate> leaveDates

) {

    @AssertTrue(message = "End time must be after start time")
    public boolean isTimeRangeValid() {
        if (startTime == null || endTime == null) {
            return true;
        }
        return endTime.isAfter(startTime);
    }

    @AssertTrue(message = "Valid-until date must be on or after valid-from date")
    public boolean isDateRangeValid() {
        if (validFrom == null || validUntil == null) {
            return true;
        }
        return !validUntil.isBefore(validFrom);
    }
}
