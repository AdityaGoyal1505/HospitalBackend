package com.example.Hospital.DTO.response;

import com.example.Hospital.model.enums.ServiceType;

import java.math.BigDecimal;

public record HospitalServiceResponse(

        Long id,
        String name,
        ServiceType type,
        String description,
        BigDecimal price,
        Long departmentId,
        String departmentName,
        boolean active

) {}
