package com.example.Hospital.Controller;

import com.example.Hospital.Config.CurrentUser;
import com.example.Hospital.DTO.DocumentDownloadDTO;
import com.example.Hospital.DTO.PatientDocumentResponseDTO;
import com.example.Hospital.Service.PatientDocumentService;
import com.example.Hospital.exception.ResourceNotFoundException;
import com.example.Hospital.model.enums.DocumentType;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class PatientDocumentController {

    private final PatientDocumentService documentService;

    // ---------- who may touch what ----------

    // Staff may use any patient's documents. A patient only their own.
    private static boolean canAccess(Long patientId) {
        return CurrentUser.isStaff() || CurrentUser.id().equals(patientId);
    }

    // For addresses that contain a patient id.
    private static void requirePatientAccess(Long patientId) {
        if (!canAccess(patientId)) {
            throw new AccessDeniedException("You can only access your own documents");
        }
    }

    // For addresses that contain a document id. Someone else's document looks like "not found".
    private PatientDocumentResponseDTO requireDocumentAccess(Long documentId) {
        PatientDocumentResponseDTO document = documentService.getById(documentId);
        if (!canAccess(document.getPatientId())) {
            throw new ResourceNotFoundException("Document not found with id " + documentId);
        }
        return document;
    }

    // ---------- endpoints ----------

    @PostMapping(value = "/api/patients/{patientId}/documents",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PatientDocumentResponseDTO> upload(
            @PathVariable Long patientId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") DocumentType documentType) {
        requirePatientAccess(patientId);
        // The uploader is always the logged-in user.
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.upload(patientId, CurrentUser.id(), documentType, file));
    }

    @GetMapping("/api/patients/{patientId}/documents")
    public ResponseEntity<Page<PatientDocumentResponseDTO>> list(
            @PathVariable Long patientId,
            @RequestParam(name = "documentType", required = false) DocumentType documentType,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        requirePatientAccess(patientId);
        return ResponseEntity.ok(documentService.list(patientId, documentType, pageable));
    }

    @GetMapping("/api/documents/{id}")
    public ResponseEntity<PatientDocumentResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(requireDocumentAccess(id));
    }

    @GetMapping("/api/documents/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        requireDocumentAccess(id); // the check comes BEFORE the file is loaded
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

    // Medical records: only an admin may remove one.
    @DeleteMapping("/api/documents/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        documentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}