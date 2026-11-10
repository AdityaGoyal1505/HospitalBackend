package com.example.Hospital.Controller;

import com.example.Hospital.DTO.request.CreateDoctorRequest;
import com.example.Hospital.DTO.request.UpdateDoctorRequest;
import com.example.Hospital.DTO.response.DoctorResponse;
import com.example.Hospital.Service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public DoctorResponse createDoctor(@Valid @RequestBody CreateDoctorRequest request) {
        return doctorService.createDoctor(request);
    }

    // List all doctors or filter by query parameters: ?name=...&specialization=...
    @GetMapping
    public List<DoctorResponse> getDoctors(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String specialization) {
        return doctorService.searchDoctors(name, specialization);
    }

    // Service endpoint around ID
    @GetMapping("/{id}")
    public DoctorResponse getDoctorById(@PathVariable Long id) {
        return doctorService.getDoctorById(id);
    }

    // Service endpoint around Name
    @GetMapping("/name/{name}")
    public List<DoctorResponse> getDoctorsByName(@PathVariable String name) {
        return doctorService.getDoctorsByName(name);
    }

    // Service endpoint around Specialization
    @GetMapping("/specialization/{specialization}")
    public List<DoctorResponse> getDoctorsBySpecialization(@PathVariable String specialization) {
        return doctorService.getDoctorsBySpecialization(specialization);
    }

    // Update by ID
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public DoctorResponse updateDoctor(@PathVariable Long id, @Valid @RequestBody UpdateDoctorRequest request) {
        return doctorService.updateDoctor(id, request);
    }

    // Update doctor image URL
    @PatchMapping("/{id}/image")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public DoctorResponse updateDoctorImageUrl(
            @PathVariable Long id,
            @RequestBody(required = false) java.util.Map<String, String> body) {
        String imageUrl = null;
        if (body != null) {
            imageUrl = body.containsKey("imageUrl") ? body.get("imageUrl") : body.get("image_url");
        }
        return doctorService.updateDoctorImageUrl(id, imageUrl);
    }

    // Delete by ID
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
    }
}
