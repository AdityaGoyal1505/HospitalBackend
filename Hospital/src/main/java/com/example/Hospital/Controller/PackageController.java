package com.example.Hospital.Controller;

import com.example.Hospital.DTO.request.CreatePackageRequest;
import com.example.Hospital.DTO.request.UpdatePackageRequest;
import com.example.Hospital.DTO.response.PackageResponse;
import com.example.Hospital.Service.PackageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/packages")
public class PackageController {

    private final PackageService packageService;

    public PackageController(PackageService packageService) {
        this.packageService = packageService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public PackageResponse createPackage(@Valid @RequestBody CreatePackageRequest request) {
        return packageService.createPackage(request);
    }

    @GetMapping
    public List<PackageResponse> getAllPackages(@RequestParam(defaultValue = "true") boolean activeOnly) {
        return packageService.getAllPackages(activeOnly);
    }

    @GetMapping("/{id}")
    public PackageResponse getPackageById(@PathVariable Long id) {
        return packageService.getPackageById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public PackageResponse updatePackage(@PathVariable Long id, @Valid @RequestBody UpdatePackageRequest request) {
        return packageService.updatePackage(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public PackageResponse updatePackageStatus(@PathVariable Long id, @RequestParam boolean active) {
        return packageService.updatePackageStatus(id, active);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public void deletePackage(@PathVariable Long id) {
        packageService.deletePackage(id);
    }
}
