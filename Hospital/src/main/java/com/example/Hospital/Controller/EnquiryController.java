package com.example.Hospital.controller;

import com.example.Hospital.DTO.request.CreatePackageEnquiryRequest;
import com.example.Hospital.DTO.request.UpdatePackageEnquiryStatusRequest;
import com.example.Hospital.DTO.response.PackageEnquiryResponse;
import com.example.Hospital.Service.PackageEnquiryService;
import com.example.Hospital.dto.EnquiryRequestDTO;
import com.example.Hospital.dto.EnquiryResponseDTO;
import com.example.Hospital.dto.EnquiryStatusUpdateRequestDTO;
import com.example.Hospital.model.enums.EnquiryStatus;
import com.example.Hospital.service.EnquiryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enquiries")
@RequiredArgsConstructor
public class EnquiryController {

    private final EnquiryService enquiryService;
    private final PackageEnquiryService packageEnquiryService;

    @PostMapping
    public ResponseEntity<EnquiryResponseDTO> create(@Valid @RequestBody EnquiryRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(enquiryService.create(request));
    }

    @PostMapping("/package")
    public ResponseEntity<PackageEnquiryResponse> createPackageEnquiry(@Valid @RequestBody CreatePackageEnquiryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(packageEnquiryService.createEnquiry(request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<PackageEnquiryResponse>> getMyPackageEnquiries() {
        return ResponseEntity.ok(packageEnquiryService.getMyEnquiries());
    }

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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        enquiryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

