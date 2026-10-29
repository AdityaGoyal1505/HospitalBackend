package com.example.Hospital.Controller;

import com.example.Hospital.DTO.request.CreateDoctorScheduleRequest;
import com.example.Hospital.DTO.request.UpdateDoctorScheduleRequest;
import com.example.Hospital.DTO.response.DoctorScheduleResponse;
import com.example.Hospital.Service.DoctorScheduleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctor-schedules")
public class DoctorScheduleController {

    private final DoctorScheduleService scheduleService;

    public DoctorScheduleController(DoctorScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public DoctorScheduleResponse createSchedule(@Valid @RequestBody CreateDoctorScheduleRequest request) {
        return scheduleService.createSchedule(request);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<DoctorScheduleResponse> getAllSchedules() {
        return scheduleService.getAllSchedules();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public DoctorScheduleResponse getScheduleById(@PathVariable Long id) {
        return scheduleService.getScheduleById(id);
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("isAuthenticated()")
    public List<DoctorScheduleResponse> getSchedulesByDoctor(@PathVariable Long doctorId) {
        return scheduleService.getSchedulesByDoctor(doctorId);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public DoctorScheduleResponse updateSchedule(@PathVariable Long id, @Valid @RequestBody UpdateDoctorScheduleRequest request) {
        return scheduleService.updateSchedule(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public DoctorScheduleResponse updateStatus(@PathVariable Long id, @RequestParam boolean active) {
        return scheduleService.updateStatus(id, active);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public void deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
    }
}
