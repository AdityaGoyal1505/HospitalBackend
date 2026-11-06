package com.example.Hospital.model;

import com.example.Hospital.model.enums.DocumentType;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "patient_document",
        indexes = @Index(name = "idx_patient_document_patient", columnList = "patient_id"))
@Getter
@Setter
public class PatientDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "uploaded_by_id", nullable = false)
    private Long uploadedById;


    @Column(name = "file_path", nullable = false, unique = true, length = 255)
    private String filePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 20)
    private DocumentType documentType;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
