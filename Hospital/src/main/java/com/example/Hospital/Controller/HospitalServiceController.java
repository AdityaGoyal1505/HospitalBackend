package com.example.Hospital.Controller;

import com.example.Hospital.DTO.request.CreateHospitalServiceRequest;
import com.example.Hospital.DTO.request.UpdateHospitalServiceRequest;
import com.example.Hospital.DTO.response.HospitalServiceResponse;
import com.example.Hospital.Service.HospitalServiceService;
import com.example.Hospital.model.enums.ServiceType;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospital-services")
public class HospitalServiceController {

    private final HospitalServiceService hospitalServiceService;

    public HospitalServiceController(HospitalServiceService hospitalServiceService) {
        this.hospitalServiceService = hospitalServiceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public HospitalServiceResponse createService(@Valid @RequestBody CreateHospitalServiceRequest request) {
        return hospitalServiceService.createService(request);
    }

    @GetMapping
    public List<HospitalServiceResponse> getServices(
            @RequestParam(required = false) ServiceType type,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        return hospitalServiceService.getServices(type, departmentId, activeOnly);
    }

    @GetMapping("/{id}")
    public HospitalServiceResponse getServiceById(@PathVariable Long id) {
        return hospitalServiceService.getServiceById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public HospitalServiceResponse updateService(@PathVariable Long id, @Valid @RequestBody UpdateHospitalServiceRequest request) {
        return hospitalServiceService.updateService(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public HospitalServiceResponse updateServiceStatus(@PathVariable Long id, @RequestParam boolean active) {
        return hospitalServiceService.updateServiceStatus(id, active);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteService(@PathVariable Long id) {
        hospitalServiceService.deleteService(id);
    }
}
