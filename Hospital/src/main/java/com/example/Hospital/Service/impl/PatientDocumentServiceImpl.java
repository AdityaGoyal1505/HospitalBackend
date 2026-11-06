package com.example.Hospital.service.impl;

import com.example.Hospital.dto.DocumentDownloadDTO;
import com.example.Hospital.dto.PatientDocumentResponseDTO;
import com.example.Hospital.model.PatientDocument;
import com.example.Hospital.model.enums.DocumentType;
import com.example.Hospital.exception.ResourceNotFoundException;
import com.example.Hospital.exception.StorageException;
import com.example.Hospital.repository.PatientDocumentRepository;
import com.example.Hospital.service.FileStorageService;
import com.example.Hospital.service.PatientDocumentService;
import java.io.IOException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PatientDocumentServiceImpl implements PatientDocumentService {

    private final PatientDocumentRepository documentRepository;
    private final FileStorageService fileStorageService;

    @Value("${app.documents.max-size-bytes}")
    private long maxSizeBytes;

    // The only file kinds we accept.
    private enum FileKind {
        PDF("pdf", "application/pdf"),
        JPEG("jpg", "image/jpeg"),
        PNG("png", "image/png");

        final String extension;
        final String contentType;

        FileKind(String extension, String contentType) {
            this.extension = extension;
            this.contentType = contentType;
        }
    }

    private PatientDocumentResponseDTO toResponse(PatientDocument document) {
        PatientDocumentResponseDTO dto = new PatientDocumentResponseDTO();
        dto.setId(document.getId());
        dto.setPatientId(document.getPatientId());
        dto.setUploadedById(document.getUploadedById());
        dto.setDocumentType(document.getDocumentType());
        dto.setOriginalFilename(document.getOriginalFilename());
        dto.setContentType(document.getContentType());
        dto.setFileSize(document.getFileSize());
        dto.setCreatedAt(document.getCreatedAt());
        return dto;
    }

    // Compares the first bytes of the file with the expected "signature" bytes.
    private static boolean startsWith(byte[] data, int... expected) {
        if (data.length < expected.length) {
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            if ((data[i] & 0xFF) != expected[i]) {
                return false;
            }
        }
        return true;
    }

    // Reads the first 8 bytes and decides what the file REALLY is.
    private FileKind detectKind(MultipartFile file) {
        if (file.getSize() < 16) {
            throw new IllegalArgumentException("Uploaded file is too small or corrupt");
        }
        byte[] head;
        try (InputStream in = file.getInputStream()) {
            head = in.readNBytes(8);
        } catch (IOException e) {
            throw new StorageException("Could not read the uploaded file", e);
        }

        if (startsWith(head, 0x25, 0x50, 0x44, 0x46, 0x2D)) {              // %PDF-
            return FileKind.PDF;
        }
        if (startsWith(head, 0xFF, 0xD8, 0xFF)) {                          // JPEG
            return FileKind.JPEG;
        }
        if (startsWith(head, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)) { // PNG
            return FileKind.PNG;
        }
        throw new IllegalArgumentException("Only PDF, JPG and PNG files are allowed");
    }

    // Keeps only a safe display name. It is never used as a name on disk.
    private static String safeFilename(String raw, String extension) {
        String name = (raw == null) ? "" : raw;
        name = name.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1);
        name = name.replaceAll("\\p{Cntrl}", "").trim();
        if (name.isEmpty()) {
            name = "document." + extension;
        }
        if (name.length() > 255) {
            name = name.substring(0, 255);
        }
        return name;
    }

    @Override
    @Transactional
    public PatientDocumentResponseDTO upload(
            Long patientId, Long uploadedById, DocumentType documentType, MultipartFile file) {

        if (patientId == null || patientId <= 0) {
            throw new IllegalArgumentException("Patient id must be positive");
        }
        if (uploadedById == null || uploadedById <= 0) {
            throw new IllegalArgumentException("Uploader id must be positive");
        }
        if (documentType == null) {
            throw new IllegalArgumentException("Document type is required");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("A file is required");
        }
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException(
                    "File is too large. Maximum size is " + (maxSizeBytes / 1_048_576) + " MB");
        }

        FileKind kind = detectKind(file);

        String key;
        try {
            key = fileStorageService.store(file.getInputStream(), kind.extension);
        } catch (IOException e) {
            throw new StorageException("Could not read the uploaded file", e);
        }

        PatientDocument document = new PatientDocument();
        document.setPatientId(patientId);
        document.setUploadedById(uploadedById);
        document.setFilePath(key);
        document.setDocumentType(documentType);
        document.setOriginalFilename(safeFilename(file.getOriginalFilename(), kind.extension));
        document.setContentType(kind.contentType);
        document.setFileSize(file.getSize());

        try {
            return toResponse(documentRepository.save(document));
        } catch (RuntimeException e) {
            fileStorageService.delete(key);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PatientDocumentResponseDTO getById(Long id) {
        PatientDocument document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id " + id));
        return toResponse(document);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientDocumentResponseDTO> list(
            Long patientId, DocumentType documentType, Pageable pageable) {

        if (patientId == null || patientId <= 0) {
            throw new IllegalArgumentException("Patient id must be positive");
        }

        Page<PatientDocument> result = (documentType == null)
                ? documentRepository.findByPatientId(patientId, pageable)
                : documentRepository.findByPatientIdAndDocumentType(patientId, documentType, pageable);

        return result.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentDownloadDTO download(Long documentId) {
        PatientDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Document not found with id " + documentId));

        Resource resource = fileStorageService.load(document.getFilePath());

        return new DocumentDownloadDTO(
                resource,
                document.getOriginalFilename(),
                document.getContentType(),
                document.getFileSize());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        PatientDocument document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id " + id));
        fileStorageService.delete(document.getFilePath());
        documentRepository.delete(document);
    }
}

