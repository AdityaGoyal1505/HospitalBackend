package com.example.Hospital.DTO.request;

import com.example.Hospital.model.enums.ServiceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateHospitalServiceRequest(

        @NotBlank(message = "Service name is required")
        @Size(max = 150, message = "Name cannot exceed 150 characters")
        String name,

        @NotNull(message = "Service type is required")
        ServiceType type,

        String description,

        @Positive(message = "Department ID must be positive")
        Long departmentId,

        @DecimalMin(value = "0.00", message = "Price cannot be negative")
        BigDecimal price

) {}
