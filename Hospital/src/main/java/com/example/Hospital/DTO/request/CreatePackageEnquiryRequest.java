package com.example.Hospital.DTO.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreatePackageEnquiryRequest(

        @Size(max = 120, message = "Name cannot exceed 120 characters")
        String name,

        @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Phone must be a valid format with 7 to 15 digits")
        String phone,

        @Email(message = "Invalid email format")
        @Size(max = 180, message = "Email cannot exceed 180 characters")
        String email,

        @NotBlank(message = "Package name is required")
        @Size(max = 150, message = "Package name cannot exceed 150 characters")
        String packageName,

        Long packageId,

        @Size(max = 2000, message = "Message cannot exceed 2000 characters")
        String message

) {}
