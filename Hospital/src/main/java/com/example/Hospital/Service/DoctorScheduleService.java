package com.example.Hospital.Service;

import com.example.Hospital.DTO.request.CreateDoctorScheduleRequest;
import com.example.Hospital.DTO.request.UpdateDoctorScheduleRequest;
import com.example.Hospital.DTO.response.DoctorScheduleResponse;
import com.example.Hospital.Repository.DoctorScheduleRepository;
import com.example.Hospital.Repository.UserRepository;
import com.example.Hospital.model.DoctorSchedule;
import com.example.Hospital.model.User;
import com.example.Hospital.model.enums.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DoctorScheduleService {

    private final DoctorScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    public DoctorScheduleService(
            DoctorScheduleRepository scheduleRepository,
            UserRepository userRepository
    ) {
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public DoctorScheduleResponse createSchedule(
            CreateDoctorScheduleRequest request
    ) {
        validateDates(
                request.validFrom(),
                request.validUntil(),
                request.blockedDates(),
                request.leaveDates()
        );

        User doctor = userRepository.findById(request.doctorId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Doctor not found"
                ));

        if (doctor.getRole() != UserRole.DOCTOR) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Selected user is not a doctor"
            );
        }

        DoctorSchedule schedule = new DoctorSchedule();
        schedule.setDoctor(doctor);
        schedule.setDayOfWeek(request.dayOfWeek());
        schedule.setStartTime(request.startTime());
        schedule.setEndTime(request.endTime());
        schedule.setValidFrom(request.validFrom());
        schedule.setValidUntil(request.validUntil());
        schedule.setBlockedDates(
                request.blockedDates() == null
                        ? new HashSet<>()
                        : new HashSet<>(request.blockedDates())
        );
        schedule.setLeaveDates(
                request.leaveDates() == null
                        ? new HashSet<>()
                        : new HashSet<>(request.leaveDates())
        );
        schedule.setActive(true);

        validateNoOverlap(schedule, null);

        DoctorSchedule saved = scheduleRepository.save(schedule);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DoctorScheduleResponse> getAllSchedules() {
        return scheduleRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DoctorScheduleResponse getScheduleById(Long id) {
        return toResponse(findSchedule(id));
    }

    @Transactional(readOnly = true)
    public List<DoctorScheduleResponse> getSchedulesByDoctor(
            Long doctorId
    ) {
        if (!userRepository.existsById(doctorId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Doctor not found"
            );
        }

        return scheduleRepository.findByDoctor_IdAndActiveTrue(doctorId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DoctorScheduleResponse updateSchedule(
            Long id,
            UpdateDoctorScheduleRequest request
    ) {
        DoctorSchedule schedule = findSchedule(id);

        validateDates(
                request.validFrom(),
                request.validUntil(),
                request.blockedDates(),
                request.leaveDates()
        );

        schedule.setDayOfWeek(request.dayOfWeek());
        schedule.setStartTime(request.startTime());
        schedule.setEndTime(request.endTime());
        schedule.setValidFrom(request.validFrom());
        schedule.setValidUntil(request.validUntil());

        schedule.setBlockedDates(
                request.blockedDates() == null
                        ? new HashSet<>()
                        : new HashSet<>(request.blockedDates())
        );
        schedule.setLeaveDates(
                request.leaveDates() == null
                        ? new HashSet<>()
                        : new HashSet<>(request.leaveDates())
        );

        validateNoOverlap(schedule, id);

        return toResponse(scheduleRepository.save(schedule));
    }

    @Transactional
    public DoctorScheduleResponse updateStatus(
            Long id,
            boolean active
    ) {
        DoctorSchedule schedule = findSchedule(id);

        if (active) {
            validateNoOverlap(schedule, id);
        }

        schedule.setActive(active);
        return toResponse(scheduleRepository.save(schedule));
    }

    @Transactional
    public void deleteSchedule(Long id) {
        DoctorSchedule schedule = findSchedule(id);
        scheduleRepository.delete(schedule);
    }

    private DoctorSchedule findSchedule(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Doctor schedule not found"
                ));
    }

    private void validateDates(
            LocalDate validFrom,
            LocalDate validUntil,
            Set<LocalDate> blockedDates,
            Set<LocalDate> leaveDates
    ) {
        if (validFrom != null
                && validUntil != null
                && validUntil.isBefore(validFrom)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Valid-until date must be on or after valid-from date"
            );
        }

        validateDateSet(blockedDates, validFrom, validUntil, "Blocked");
        validateDateSet(leaveDates, validFrom, validUntil, "Leave");
    }

    private void validateDateSet(
            Set<LocalDate> dates,
            LocalDate validFrom,
            LocalDate validUntil,
            String label
    ) {
        if (dates == null) {
            return;
        }

        for (LocalDate date : dates) {
            if (date == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        label + " dates cannot contain null"
                );
            }

            if (validFrom != null && date.isBefore(validFrom)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        label + " date cannot be before valid-from"
                );
            }

            if (validUntil != null && date.isAfter(validUntil)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        label + " date cannot be after valid-until"
                );
            }
        }
    }

    private void validateNoOverlap(
            DoctorSchedule candidate,
            Long excludedId
    ) {
        if (!candidate.isActive()) {
            return;
        }

        List<DoctorSchedule> existingSchedules =
                scheduleRepository
                        .findByDoctor_IdAndDayOfWeekAndActiveTrue(
                                candidate.getDoctor().getId(),
                                candidate.getDayOfWeek()
                        );

        for (DoctorSchedule existing : existingSchedules) {
            if (excludedId != null
                    && existing.getId().equals(excludedId)) {
                continue;
            }

            boolean timesOverlap =
                    candidate.getStartTime().isBefore(existing.getEndTime())
                            && existing.getStartTime()
                            .isBefore(candidate.getEndTime());

            boolean datesOverlap = dateRangesOverlap(
                    candidate.getValidFrom(),
                    candidate.getValidUntil(),
                    existing.getValidFrom(),
                    existing.getValidUntil()
            );

            if (timesOverlap && datesOverlap) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "This schedule overlaps with another active schedule "
                                + "for the same doctor and day"
                );
            }
        }
    }

    private boolean dateRangesOverlap(
            LocalDate start1,
            LocalDate end1,
            LocalDate start2,
            LocalDate end2
    ) {
        boolean firstEndsAfterSecondStarts =
                end1 == null || start2 == null
                        || !end1.isBefore(start2);

        boolean secondEndsAfterFirstStarts =
                end2 == null || start1 == null
                        || !end2.isBefore(start1);

        return firstEndsAfterSecondStarts
                && secondEndsAfterFirstStarts;
    }

    private DoctorScheduleResponse toResponse(
            DoctorSchedule schedule
    ) {
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
