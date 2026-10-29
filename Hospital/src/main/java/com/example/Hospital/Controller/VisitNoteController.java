package com.example.Hospital.Controller;

import com.example.Hospital.DTO.request.CreateVisitNoteRequest;
import com.example.Hospital.DTO.request.UpdateVisitNoteRequest;
import com.example.Hospital.DTO.response.VisitNoteResponse;
import com.example.Hospital.Service.VisitNoteService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/visit-notes")
public class VisitNoteController {

    private final VisitNoteService visitNoteService;

    public VisitNoteController(VisitNoteService visitNoteService) {
        this.visitNoteService = visitNoteService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('DOCTOR')")
    public VisitNoteResponse createVisitNote(@Valid @RequestBody CreateVisitNoteRequest request) {
        return visitNoteService.createVisitNote(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'PATIENT')")
    public VisitNoteResponse getVisitNoteById(@PathVariable Long id) {
        return visitNoteService.getVisitNoteById(id);
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    public List<VisitNoteResponse> getVisitNotesByPatientId(@PathVariable Long patientId) {
        return visitNoteService.getVisitNotesByPatientId(patientId);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('DOCTOR')")
    public List<VisitNoteResponse> getMyVisitNotes() {
        return visitNoteService.getMyVisitNotes();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public VisitNoteResponse updateVisitNote(@PathVariable Long id, @Valid @RequestBody UpdateVisitNoteRequest request) {
        return visitNoteService.updateVisitNote(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('DOCTOR')")
    public void deleteVisitNote(@PathVariable Long id) {
        visitNoteService.deleteVisitNote(id);
    }
}
