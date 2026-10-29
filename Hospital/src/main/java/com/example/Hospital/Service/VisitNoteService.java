package com.example.Hospital.Service;

import com.example.Hospital.DTO.request.CreateVisitNoteRequest;
import com.example.Hospital.DTO.request.UpdateVisitNoteRequest;
import com.example.Hospital.DTO.response.VisitNoteResponse;
import com.example.Hospital.Repository.AppointmentRepository;
import com.example.Hospital.Repository.UserRepository;
import com.example.Hospital.Repository.VisitNoteRepository;
import com.example.Hospital.model.Appointment;
import com.example.Hospital.model.User;
import com.example.Hospital.model.VisitNote;
import com.example.Hospital.model.enums.AppointmentStatus;
import com.example.Hospital.model.enums.UserRole;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class VisitNoteService {

    private final VisitNoteRepository visitNoteRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    public VisitNoteService(VisitNoteRepository visitNoteRepository, AppointmentRepository appointmentRepository, UserRepository userRepository) {
        this.visitNoteRepository = visitNoteRepository;
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public VisitNoteResponse createVisitNote(CreateVisitNoteRequest request) {
        User doctor = getAuthenticatedUser();

        if (doctor.getRole() != UserRole.DOCTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only doctors can create visit notes");
        }

        if (request.getAppointmentId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment ID is required");
        }

        Appointment appointment = appointmentRepository
                .findById(request.getAppointmentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        if (appointment.getDoctor() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No doctor is assigned to this appointment");
        }

        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only add notes to your own appointments");
        }

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED
                && appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Visit notes cannot be added to a cancelled appointment");
        }

        if (visitNoteRepository.existsByAppointment_Id(appointment.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A visit note already exists for this appointment");
        }

        VisitNote note = new VisitNote();
        note.setAppointment(appointment);
        note.setPatient(appointment.getPatient());
        note.setDoctor(doctor);
        note.setNotes(request.getNotes().trim());

        VisitNote saved = visitNoteRepository.save(note);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public VisitNoteResponse getVisitNoteById(Long id) {
        User user = getAuthenticatedUser();

        VisitNote note = visitNoteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Visit note not found"));

        boolean isDoctorOwner = user.getRole() == UserRole.DOCTOR && note.getDoctor().getId().equals(user.getId());
        boolean isPatientOwner = user.getRole() == UserRole.PATIENT && note.getPatient().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == UserRole.ADMIN;

        if (!isDoctorOwner && !isPatientOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to view this visit note");
        }

        return toResponse(note);
    }

    @Transactional(readOnly = true)
    public List<VisitNoteResponse> getVisitNotesByPatientId(Long patientId) {
        User user = getAuthenticatedUser();

        boolean isAdmin = user.getRole() == UserRole.ADMIN;
        boolean isOwnPatient = user.getRole() == UserRole.PATIENT && user.getId().equals(patientId);

        if (!isAdmin && !isOwnPatient && user.getRole() != UserRole.DOCTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to view these visit notes");
        }

        List<VisitNote> notes = visitNoteRepository.findByPatient_IdOrderByCreatedAtDesc(patientId);

        if (user.getRole() == UserRole.DOCTOR) {
            notes = notes.stream()
                    .filter(note -> note.getDoctor().getId().equals(user.getId()))
                    .toList();
        }

        return notes.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<VisitNoteResponse> getMyVisitNotes() {
        User doctor = getAuthenticatedUser();

        if (doctor.getRole() != UserRole.DOCTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only doctors can access their visit notes");
        }

        return visitNoteRepository
                .findByDoctor_IdOrderByCreatedAtDesc(doctor.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public VisitNoteResponse updateVisitNote(Long id, UpdateVisitNoteRequest request) {
        User doctor = getAuthenticatedUser();

        if (doctor.getRole() != UserRole.DOCTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only doctors can update visit notes");
        }

        VisitNote note = visitNoteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Visit note not found"));

        if (!note.getDoctor().getId().equals(doctor.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update visit notes you authored");
        }

        if (note.getAppointment() != null
                && note.getAppointment().getStatus()
                == AppointmentStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Visit notes for cancelled appointments cannot be updated");
        }

        note.setNotes(request.getNotes().trim());

        VisitNote updated = visitNoteRepository.save(note);

        return toResponse(updated);
    }

    @Transactional
    public void deleteVisitNote(Long id) {
        User doctor = getAuthenticatedUser();

        if (doctor.getRole() != UserRole.DOCTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only doctors can delete visit notes");
        }

        VisitNote note = visitNoteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Visit note not found"));

        if (!note.getDoctor().getId().equals(doctor.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete visit notes you authored");
        }

        visitNoteRepository.delete(note);
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication");
        }

        final Long userId;

        try {
            userId = Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT subject is not a valid user ID");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user was not found"));
    }

    private VisitNoteResponse toResponse(VisitNote note) {
        return VisitNoteResponse.builder()
                .id(note.getId())
                .patientId(note.getPatient().getId())
                .patientName(note.getPatient().getName())
                .doctorId(note.getDoctor().getId())
                .doctorName(note.getDoctor().getName())
                .appointmentId(note.getAppointment() != null ? note.getAppointment().getId() : null)
                .notes(note.getNotes())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}
