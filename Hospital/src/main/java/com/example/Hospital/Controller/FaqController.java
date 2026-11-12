package com.example.Hospital.controller;

import com.example.Hospital.dto.FaqRequestDTO;
import com.example.Hospital.dto.FaqResponseDTO;
import com.example.Hospital.dto.FaqUpdateRequestDTO;
import com.example.Hospital.service.FaqService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/faqs")
@RequiredArgsConstructor
public class FaqController {

    private final FaqService faqService;

    @GetMapping
    public ResponseEntity<Page<FaqResponseDTO>> getAllActive(
            @RequestParam(name = "category", required = false) String category,
            @PageableDefault(size = 20, sort = "faqId") Pageable pageable) {
        return ResponseEntity.ok(faqService.getAllActive(category, pageable));
    }

    @GetMapping("/{faqId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FaqResponseDTO> getById(@PathVariable Long faqId) {
        return ResponseEntity.ok(faqService.getById(faqId));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<FaqResponseDTO>> getAllForAdmin(
            @PageableDefault(size = 20, sort = "faqId") Pageable pageable) {
        return ResponseEntity.ok(faqService.getAllForAdmin(pageable));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FaqResponseDTO> create(@Valid @RequestBody FaqRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(faqService.create(request));
    }

    @PatchMapping("/{faqId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FaqResponseDTO> update(
            @PathVariable Long faqId,
            @Valid @RequestBody FaqUpdateRequestDTO request) {
        return ResponseEntity.ok(faqService.update(faqId, request));
    }

    @PatchMapping("/{faqId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable Long faqId) {
        faqService.deactivate(faqId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{faqId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activate(@PathVariable Long faqId) {
        faqService.activate(faqId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{faqId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long faqId) {
        faqService.delete(faqId);
        return ResponseEntity.noContent().build();
    }
}
