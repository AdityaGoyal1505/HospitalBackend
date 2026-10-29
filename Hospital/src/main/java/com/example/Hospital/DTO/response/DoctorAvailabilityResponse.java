
package com.example.Hospital.DTO.response;

import java.time.LocalDate;
import java.time.Instant;
import java.util.List;

public record DoctorAvailabilityResponse(Long doctorId, String doctorName, LocalDate date, String timeZone, List<AvailableSlot> availableSlots) {
    public record AvailableSlot(Instant startAt, Instant endAt, String startTime, String endTime) {}
}
