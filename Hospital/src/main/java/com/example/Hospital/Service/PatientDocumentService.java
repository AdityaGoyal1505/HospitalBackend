package com.example.Hospital.service;

import com.example.Hospital.dto.DocumentDownloadDTO;
import com.example.Hospital.dto.PatientDocumentResponseDTO;
import com.example.Hospital.model.enums.DocumentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface PatientDocumentService {

    PatientDocumentResponseDTO upload(
            Long patientId, Long uploadedById, DocumentType documentType, MultipartFile file);

    PatientDocumentResponseDTO getById(Long id);

    Page<PatientDocumentResponseDTO> list(
            Long patientId, DocumentType documentType, Pageable pageable);

    DocumentDownloadDTO download(Long documentId);

    void delete(Long id);
}

