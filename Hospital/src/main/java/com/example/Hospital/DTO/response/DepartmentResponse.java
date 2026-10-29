
package com.example.Hospital.DTO.response;

import com.example.Hospital.model.enums.DepartmentType;

public record DepartmentResponse(

        Long id,
        DepartmentType name,
        String description,
        String imageUrl,
        boolean active

) {}
