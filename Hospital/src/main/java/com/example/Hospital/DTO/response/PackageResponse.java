package com.example.Hospital.DTO.response;

import java.math.BigDecimal;
import java.time.Instant;

public record PackageResponse(

        Long id,
        String name,
        String description,
        BigDecimal price,
        boolean active,
        Instant createdAt,
        Instant updatedAt

) {}
