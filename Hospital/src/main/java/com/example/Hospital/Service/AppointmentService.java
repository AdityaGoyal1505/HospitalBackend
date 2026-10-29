package com.example.Hospital.Service;

import com.example.Hospital.DTO.request.CreateAppointmentRequest;
import com.example.Hospital.DTO.request.UpdateAppointmentRequest;
import com.example.Hospital.DTO.request.UpdateAppointmentStatusRequest;
import com.example.Hospital.DTO.response.AppointmentResponse;
import com.example.Hospital.DTO.response.DoctorAvailabilityResponse;
import com.example.Hospital.Repository.AppointmentRepository;
import com.example.Hospital.Repository.DoctorScheduleRepository;
import com.example.Hospital.Repository.HospitalServiceRepository;
import com.example.Hospital.Repository.UserRepository;
import com.example.Hospital.model.Appointment;
import com.example.Hospital.model.DoctorSchedule;
import com.example.Hospital.model.HospitalService;
import com.example.Hospital.model.User;
import com.example.Hospital.model.enums.AppointmentStatus;
import com.example.Hospital.model.enums.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class AppointmentService {

    private static final Duration APPOINTMENT_DURATION = Duration.ofMinutes(15);
    private static final Duration COOLDOWN = Duration.ofMinutes(15);
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Kolkata");
    private static final Set<AppointmentStatus> BLOCKING_STATUSES = Set.of(AppointmentStatus.CONFIRMED, AppointmentStatus.COMPLETED);

    private final AppointmentRepository appointmentRepository;
    private final DoctorScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final HospitalServiceRepository hospitalServiceRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              DoctorScheduleRepository scheduleRepository,
                              UserRepository userRepository,
                              HospitalServiceRepository hospitalServiceRepository) {
        this.appointmentRepository = appointmentRepository;
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
        this.hospitalServiceRepository = hospitalServiceRepository;
    }

    @Transactional
    public AppointmentResponse createAppointment(CreateAppointmentRequest request) {
        if (request.getDoctorId() == null || request.getPreferredAt() == null || request.getType() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor, appointment time, and appointment type are required");
        }

        Instant startAt = request.getPreferredAt();

        if (!startAt.isAfter(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment time must be in the future");
        }

        ZoneId zone = resolveZone(request.getPatientTimeZone());
        User patient = getAuthenticatedPatient();

        User doctor = userRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Selected doctor not found"));

        if (doctor.getRole() != UserRole.DOCTOR) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected user is not a doctor");
        }

        if (!doctor.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected doctor is inactive");
        }

        HospitalService selectedService = null;
        if (request.getServiceId() != null) {
            selectedService = hospitalServiceRepository.findById(request.getServiceId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hospital service not found"));

            if (!selectedService.isActive()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected hospital service is inactive");
            }
        }

        Instant endAt = startAt.plus(APPOINTMENT_DURATION);
        LocalDateTime localStart = LocalDateTime.ofInstant(startAt, zone);

        LocalDate appointmentDate = localStart.toLocalDate();
        LocalTime appointmentStart = localStart.toLocalTime();
        DayOfWeek dayOfWeek = appointmentDate.getDayOfWeek();

        if (localStart.getSecond() != 0 || localStart.getNano() != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Select a valid appointment time slot");
        }

        List<DoctorSchedule> schedules = scheduleRepository
                .findByDoctor_IdAndDayOfWeekAndActiveTrue(doctor.getId(), dayOfWeek);

        boolean scheduleMatches = false;
        for (DoctorSchedule schedule : schedules) {
            if (!isScheduleValidOnDate(schedule, appointmentDate)) {
                continue;
            }

            if (appointmentStart.isBefore(schedule.getStartTime())) {
                continue;
            }

            LocalTime requiredEnd = appointmentStart.plusMinutes(30);
            if (requiredEnd.isAfter(schedule.getEndTime())) {
                continue;
            }

            long minutesFromScheduleStart = ChronoUnit.MINUTES.between(schedule.getStartTime(), appointmentStart);
            if (minutesFromScheduleStart < 0 || minutesFromScheduleStart % 30 != 0) {
                continue;
            }

            scheduleMatches = true;
            break;
        }

        if (!scheduleMatches) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Selected time is outside the doctor's available schedule");
        }

        if (hasConflict(doctor.getId(), startAt)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Selected doctor already has an appointment or cooldown at this time");
        }

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setService(selectedService);
        appointment.setType(request.getType());
        appointment.setPreferredAt(startAt);
        appointment.setStartAt(startAt);
        appointment.setEndAt(endAt);
        appointment.setPatientTimeZone(zone.getId());
        appointment.setReason(request.getReason());
        appointment.setCollectionType(request.getCollectionType());
        appointment.setCollectionAddress(request.getCollectionAddress());
        appointment.setStatus(AppointmentStatus.CONFIRMED);

        return toResponse(appointmentRepository.save(appointment));
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getMyAppointments() {
        User patient = getAuthenticatedPatient();

        return appointmentRepository
                .findByPatient_IdOrderByCreatedAtDesc(patient.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getDoctorAppointments() {
        User doctor = getAuthenticatedUser();
        if (doctor.getRole() != UserRole.DOCTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only doctors can access doctor appointments");
        }

        return appointmentRepository
                .findByDoctor_IdOrderByStartAtAsc(doctor.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAllAppointments(AppointmentStatus status) {
        List<Appointment> list = (status != null)
                ? appointmentRepository.findByStatusOrderByCreatedAtDesc(status)
                : appointmentRepository.findAll();

        return list.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        User currentUser = getAuthenticatedUser();
        boolean isPatientOwner = appointment.getPatient().getId().equals(currentUser.getId());
        boolean isAssignedDoctor = appointment.getDoctor() != null && appointment.getDoctor().getId().equals(currentUser.getId());
        boolean isStaff = currentUser.getRole() == UserRole.ADMIN || currentUser.getRole() == UserRole.RECEPTION;

        if (!isPatientOwner && !isAssignedDoctor && !isStaff) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to view this appointment");
        }

        return toResponse(appointment);
    }

    @Transactional
    public AppointmentResponse updateAppointment(Long id, UpdateAppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        User currentUser = getAuthenticatedUser();
        boolean isPatientOwner = appointment.getPatient().getId().equals(currentUser.getId());
        boolean isStaff = currentUser.getRole() == UserRole.ADMIN || currentUser.getRole() == UserRole.RECEPTION;

        if (!isPatientOwner && !isStaff) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to update this appointment");
        }

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only confirmed appointments can be updated");
        }

        if (request.getType() != null) {
            appointment.setType(request.getType());
        }

        if (request.getReason() != null) {
            appointment.setReason(request.getReason());
        }

        if (request.getCollectionType() != null) {
            appointment.setCollectionType(request.getCollectionType());
        }

        if (request.getCollectionAddress() != null) {
            appointment.setCollectionAddress(request.getCollectionAddress());
        }

        if (request.getServiceId() != null) {
            HospitalService service = hospitalServiceRepository.findById(request.getServiceId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hospital service not found"));
            appointment.setService(service);
        }

        if (request.getPreferredAt() != null && !request.getPreferredAt().equals(appointment.getStartAt())) {
            Instant newStart = request.getPreferredAt();
            if (!newStart.isAfter(Instant.now())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment time must be in the future");
            }

            if (appointment.getDoctor() != null && hasConflictExcluding(appointment.getDoctor().getId(), newStart, appointment.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Selected doctor already has an appointment or cooldown at this time");
            }

            appointment.setPreferredAt(newStart);
            appointment.setStartAt(newStart);
            appointment.setEndAt(newStart.plus(APPOINTMENT_DURATION));
        }

        return toResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentResponse updateAppointmentStatus(Long id, UpdateAppointmentStatusRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        User currentUser = getAuthenticatedUser();
        boolean isAssignedDoctor = appointment.getDoctor() != null && appointment.getDoctor().getId().equals(currentUser.getId());
        boolean isStaff = currentUser.getRole() == UserRole.ADMIN || currentUser.getRole() == UserRole.RECEPTION;
        boolean isPatientOwner = appointment.getPatient().getId().equals(currentUser.getId());

        if (request.getStatus() == AppointmentStatus.CANCELLED) {
            if (!isPatientOwner && !isStaff && !isAssignedDoctor) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to cancel this appointment");
            }
        } else if (request.getStatus() == AppointmentStatus.COMPLETED) {
            if (!isAssignedDoctor && !isStaff) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only doctor or staff can complete an appointment");
            }
        }

        appointment.setStatus(request.getStatus());
        if (request.getCancellationReason() != null) {
            appointment.setCancellationReason(request.getCancellationReason());
        }

        return toResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentResponse cancelAppointment(Long id, String cancellationReason) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        User currentUser = getAuthenticatedUser();
        boolean isPatientOwner = appointment.getPatient().getId().equals(currentUser.getId());
        boolean isStaff = currentUser.getRole() == UserRole.ADMIN || currentUser.getRole() == UserRole.RECEPTION;

        if (!isPatientOwner && !isStaff) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to cancel this appointment");
        }

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only confirmed appointments can be cancelled");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancellationReason(cancellationReason);

        return toResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    public void deleteAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
        appointmentRepository.delete(appointment);
    }

    private boolean isScheduleValidOnDate(DoctorSchedule schedule, LocalDate date) {
        if (schedule.getValidFrom() != null && date.isBefore(schedule.getValidFrom())) {
            return false;
        }

        if (schedule.getValidUntil() != null && date.isAfter(schedule.getValidUntil())) {
            return false;
        }

        if (schedule.getBlockedDates() != null && schedule.getBlockedDates().contains(date)) {
            return false;
        }

        if (schedule.getLeaveDates() != null && schedule.getLeaveDates().contains(date)) {
            return false;
        }

        return true;
    }

    private boolean hasConflict(Long doctorId, Instant requestedStart) {
        return hasConflictExcluding(doctorId, requestedStart, null);
    }

    private boolean hasConflictExcluding(Long doctorId, Instant requestedStart, Long excludeAppointmentId) {
        Instant conflictWindowStart = requestedStart.minus(COOLDOWN);
        Instant conflictWindowEnd = requestedStart.plus(APPOINTMENT_DURATION).plus(COOLDOWN);

        List<Appointment> conflicts = appointmentRepository
                .findByDoctor_IdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
                        doctorId, BLOCKING_STATUSES, conflictWindowEnd, conflictWindowStart);

        return conflicts.stream().anyMatch(a -> excludeAppointmentId == null || !a.getId().equals(excludeAppointmentId));
    }

    private ZoneId resolveZone(String timeZone) {
        if (timeZone == null || timeZone.isBlank()) {
            return DEFAULT_ZONE;
        }

        try {
            return ZoneId.of(timeZone);
        } catch (DateTimeException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid time zone: " + timeZone);
        }
    }

    private User getAuthenticatedPatient() {
        User user = getAuthenticatedUser();
        if (user.getRole() != UserRole.PATIENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only patients can book appointments");
        }
        return user;
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication");
        }

        String subject = jwt.getSubject();
        final Long userId;

        try {
            userId = Long.valueOf(subject);
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT subject is not a valid user ID");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user was not found"));
    }

    @Transactional(readOnly = true)
    public DoctorAvailabilityResponse getAvailableSlots(Long doctorId, LocalDate date, String timeZone) {
        ZoneId zone = resolveZone(timeZone);

        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));

        if (doctor.getRole() != UserRole.DOCTOR || !doctor.isActive()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Active doctor not found");
        }

        DayOfWeek dayOfWeek = date.getDayOfWeek();

        // FIX: Load active schedules for day of week and filter in-memory with isScheduleValidOnDate.
        // This avoids SQL NULL comparison failure for ongoing/permanent schedules with validFrom or validUntil null.
        List<DoctorSchedule> schedules = scheduleRepository
                .findByDoctor_IdAndDayOfWeekAndActiveTrue(doctorId, dayOfWeek);

        List<DoctorAvailabilityResponse.AvailableSlot> availableSlots = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now(zone);

        for (DoctorSchedule schedule : schedules) {
            if (!isScheduleValidOnDate(schedule, date)) {
                continue;
            }

            LocalTime slotStart = schedule.getStartTime();
            while (!slotStart.plusMinutes(30).isAfter(schedule.getEndTime())) {
                LocalDateTime localStart = LocalDateTime.of(date, slotStart);
                LocalDateTime localEnd = localStart.plusMinutes(15);

                if (localStart.isAfter(now)) {
                    Instant startInstant = localStart.atZone(zone).toInstant();

                    if (!hasConflict(doctorId, startInstant)) {
                        Instant endInstant = localEnd.atZone(zone).toInstant();
                        availableSlots.add(new DoctorAvailabilityResponse.AvailableSlot(
                                startInstant, endInstant, slotStart.toString(), localEnd.toLocalTime().toString()));
                    }
                }
                slotStart = slotStart.plusMinutes(30);
            }
        }

        return new DoctorAvailabilityResponse(doctorId, doctor.getName(), date, zone.getId(), availableSlots);
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .patientId(appointment.getPatient().getId())
                .patientName(appointment.getPatient().getName())
                .doctorId(appointment.getDoctor() == null ? null : appointment.getDoctor().getId())
                .doctorName(appointment.getDoctor() == null ? null : appointment.getDoctor().getName())
                .serviceId(appointment.getService() == null ? null : appointment.getService().getId())
                .serviceName(appointment.getService() == null ? null : appointment.getService().getName())
                .type(appointment.getType())
                .preferredAt(appointment.getPreferredAt())
                .startAt(appointment.getStartAt())
                .endAt(appointment.getEndAt())
                .patientTimeZone(appointment.getPatientTimeZone())
                .status(appointment.getStatus())
                .reason(appointment.getReason())
                .collectionType(appointment.getCollectionType())
                .collectionAddress(appointment.getCollectionAddress())
                .cancellationReason(appointment.getCancellationReason())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
    }
}
