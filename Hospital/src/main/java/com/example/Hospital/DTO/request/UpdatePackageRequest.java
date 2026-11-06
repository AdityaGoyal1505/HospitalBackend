package com.example.Hospital.DTO.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdatePackageRequest(

        @NotBlank(message = "Package name is required")
        @Size(max = 150, message = "Package name cannot exceed 150 characters")
        String name,

        @Size(max = 2000, message = "Description cannot exceed 2000 characters")
        String description,

        @DecimalMin(value = "0.00", message = "Price cannot be negative")
        BigDecimal price,

        Boolean active

) {}
