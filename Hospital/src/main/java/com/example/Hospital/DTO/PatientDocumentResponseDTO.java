package com.example.Hospital.dto;

import com.example.Hospital.model.enums.DocumentType;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatientDocumentResponseDTO {
    private Long id;
    private Long patientId;
    private Long uploadedById;
    private DocumentType documentType;
    private String originalFilename;
    private String contentType;
    private Long fileSize;
    private Instant createdAt;
}
