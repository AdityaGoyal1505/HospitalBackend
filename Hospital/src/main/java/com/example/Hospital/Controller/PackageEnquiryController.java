package com.example.Hospital.Controller;

import com.example.Hospital.DTO.request.CreatePackageEnquiryRequest;
import com.example.Hospital.DTO.request.UpdatePackageEnquiryStatusRequest;
import com.example.Hospital.DTO.response.PackageEnquiryResponse;
import com.example.Hospital.Service.PackageEnquiryService;
import com.example.Hospital.model.enums.PackageEnquiryStatus;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/package-enquiries", "/api/packages/enquiries"})
public class PackageEnquiryController {

    private final PackageEnquiryService enquiryService;

    public PackageEnquiryController(PackageEnquiryService enquiryService) {
        this.enquiryService = enquiryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public PackageEnquiryResponse createEnquiry(@Valid @RequestBody CreatePackageEnquiryRequest request) {
        return enquiryService.createEnquiry(request);
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public List<PackageEnquiryResponse> getMyEnquiries() {
        return enquiryService.getMyEnquiries();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public List<PackageEnquiryResponse> getAllEnquiries(@RequestParam(required = false) PackageEnquiryStatus status) {
        return enquiryService.getAllEnquiries(status);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public PackageEnquiryResponse getEnquiryById(@PathVariable Long id) {
        return enquiryService.getEnquiryById(id);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public PackageEnquiryResponse updateEnquiryStatus(@PathVariable Long id, @Valid @RequestBody UpdatePackageEnquiryStatusRequest request) {
        return enquiryService.updateEnquiryStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public void deleteEnquiry(@PathVariable Long id) {
        enquiryService.deleteEnquiry(id);
    }
}
