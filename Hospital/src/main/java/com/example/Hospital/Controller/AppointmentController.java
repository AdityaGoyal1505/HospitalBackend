package com.example.Hospital.Controller;

import com.example.Hospital.DTO.request.CreateAppointmentRequest;
import com.example.Hospital.DTO.request.UpdateAppointmentRequest;
import com.example.Hospital.DTO.request.UpdateAppointmentStatusRequest;
import com.example.Hospital.DTO.response.AppointmentResponse;
import com.example.Hospital.DTO.response.DoctorAvailabilityResponse;
import com.example.Hospital.Service.AppointmentService;
import com.example.Hospital.model.enums.AppointmentStatus;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PATIENT')")
    public AppointmentResponse createAppointment(@Valid @RequestBody CreateAppointmentRequest request) {
        return appointmentService.createAppointment(request);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public List<AppointmentResponse> getMyAppointments() {
        return appointmentService.getMyAppointments();
    }

    @GetMapping("/doctor/my")
    @PreAuthorize("hasRole('DOCTOR')")
    public List<AppointmentResponse> getDoctorAppointments() {
        return appointmentService.getDoctorAppointments();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public List<AppointmentResponse> getAllAppointments(@RequestParam(required = false) AppointmentStatus status) {
        return appointmentService.getAllAppointments(status);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public AppointmentResponse getAppointmentById(@PathVariable Long id) {
        return appointmentService.getAppointmentById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public AppointmentResponse updateAppointment(@PathVariable Long id, @Valid @RequestBody UpdateAppointmentRequest request) {
        return appointmentService.updateAppointment(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public AppointmentResponse updateAppointmentStatus(@PathVariable Long id, @Valid @RequestBody UpdateAppointmentStatusRequest request) {
        return appointmentService.updateAppointmentStatus(id, request);
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public AppointmentResponse cancelAppointment(@PathVariable Long id, @RequestParam(required = false) @Size(max = 5000) String reason) {
        return appointmentService.cancelAppointment(id, reason);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
    }

    @GetMapping("/doctors/{doctorId}/availability")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<DoctorAvailabilityResponse> getAvailableSlots(
            @PathVariable Long doctorId,
            @RequestParam LocalDate date,
            @RequestParam(required = false, defaultValue = "Asia/Kolkata") String timeZone) {
        return ResponseEntity.ok(appointmentService.getAvailableSlots(doctorId, date, timeZone));
    }
}
