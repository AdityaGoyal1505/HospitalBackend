package com.example.Hospital.Controller;

import com.example.Hospital.Config.CurrentUser;
import com.example.Hospital.DTO.EnquiryRequestDTO;
import com.example.Hospital.DTO.EnquiryResponseDTO;
import com.example.Hospital.DTO.EnquiryStatusUpdateRequestDTO;
import com.example.Hospital.DTO.request.CreatePackageEnquiryRequest;
import com.example.Hospital.DTO.request.UpdatePackageEnquiryStatusRequest;
import com.example.Hospital.DTO.response.PackageEnquiryResponse;
import com.example.Hospital.Service.EnquiryService;
import com.example.Hospital.Service.PackageEnquiryService;
import com.example.Hospital.model.enums.EnquiryStatus;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enquiries")
@RequiredArgsConstructor
public class EnquiryController {

    private final EnquiryService enquiryService;
    private final PackageEnquiryService packageEnquiryService;

    // Public. A visitor has no id (null). A logged-in user has theirs, taken from the token.
    @PostMapping
    public ResponseEntity<EnquiryResponseDTO> create(@Valid @RequestBody EnquiryRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(enquiryService.create(CurrentUser.idOrNull(), request));
    }

    // Package enquiries: any logged-in user (the security rules allow it).
    @PostMapping("/package")
    public ResponseEntity<PackageEnquiryResponse> createPackageEnquiry(
            @Valid @RequestBody CreatePackageEnquiryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(packageEnquiryService.createEnquiry(request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<PackageEnquiryResponse>> getMyPackageEnquiries() {
        return ResponseEntity.ok(packageEnquiryService.getMyEnquiries());
    }

    // The methods below are for staff (ADMIN, RECEPTION). The security rules enforce it.
    @GetMapping
    public ResponseEntity<Page<EnquiryResponseDTO>> getAll(
            @RequestParam(name = "status", required = false) EnquiryStatus status,
            @RequestParam(name = "patientId", required = false) Long patientId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(enquiryService.getAll(status, patientId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnquiryResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(enquiryService.getById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EnquiryResponseDTO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody EnquiryStatusUpdateRequestDTO request) {
        return ResponseEntity.ok(enquiryService.updateStatus(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PackageEnquiryResponse> updatePackageEnquiryStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePackageEnquiryStatusRequest request) {
        return ResponseEntity.ok(packageEnquiryService.updateEnquiryStatus(id, request));
    }

    // Only an admin may delete an enquiry.
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        enquiryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}