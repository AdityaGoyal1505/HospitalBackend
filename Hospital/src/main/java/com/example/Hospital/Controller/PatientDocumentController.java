package com.example.Hospital.controller;

import com.example.Hospital.dto.DocumentDownloadDTO;
import com.example.Hospital.dto.PatientDocumentResponseDTO;
import com.example.Hospital.model.enums.DocumentType;
import com.example.Hospital.service.PatientDocumentService;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class PatientDocumentController {

    private final PatientDocumentService documentService;

    @PostMapping(value = "/api/patients/{patientId}/documents",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PatientDocumentResponseDTO> upload(
            @PathVariable Long patientId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") DocumentType documentType,
            @RequestParam("uploadedById") Long uploadedById) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.upload(patientId, uploadedById, documentType, file));
    }

    @GetMapping("/api/patients/{patientId}/documents")
    public ResponseEntity<Page<PatientDocumentResponseDTO>> list(
            @PathVariable Long patientId,
            @RequestParam(name = "documentType", required = false) DocumentType documentType,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(documentService.list(patientId, documentType, pageable));
    }

    @GetMapping("/api/documents/{id}")
    public ResponseEntity<PatientDocumentResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.getById(id));
    }

    @GetMapping("/api/documents/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        DocumentDownloadDTO file = documentService.download(id);

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(file.filename(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .contentLength(file.size())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header("X-Content-Type-Options", "nosniff")
                .body(file.resource());
    }

    @DeleteMapping("/api/documents/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        documentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

