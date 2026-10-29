
package com.example.Hospital.DTO.request;

import com.example.Hospital.model.enums.DepartmentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record UpdateDepartmentRequest(

        @NotNull(message = "Department name is required")
        DepartmentType name,

        @Size(max = 2000, message = "Description cannot exceed 2000 characters")
        String description,

        @Size(max = 500, message = "Image URL cannot exceed 500 characters")
        @Pattern(
                regexp = "^(https?://.*)?$",
                message = "Image URL must start with http:// or https://"
        )
        String imageUrl

) {}
