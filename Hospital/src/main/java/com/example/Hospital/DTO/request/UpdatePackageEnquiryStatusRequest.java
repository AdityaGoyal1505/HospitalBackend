package com.example.Hospital.DTO.request;

import com.example.Hospital.model.enums.PackageEnquiryStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdatePackageEnquiryStatusRequest(

        @NotNull(message = "Enquiry status is required")
        PackageEnquiryStatus status,

        @Size(max = 2000, message = "Contact notes cannot exceed 2000 characters")
        String contactNotes

) {}
