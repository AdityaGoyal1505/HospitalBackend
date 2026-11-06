package com.example.Hospital.DTO.response;

import com.example.Hospital.model.enums.PackageEnquiryStatus;

import java.time.Instant;

public record PackageEnquiryResponse(

        Long id,
        Long userId,
        String name,
        String phone,
        String email,
        String packageName,
        Long packageId,
        String message,
        PackageEnquiryStatus status,
        String contactNotes,
        Long contactedByUserId,
        String contactedByUserName,
        Instant contactedAt,
        Instant createdAt,
        Instant updatedAt

) {}
