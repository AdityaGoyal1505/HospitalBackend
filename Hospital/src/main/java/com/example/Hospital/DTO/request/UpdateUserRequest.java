package com.example.Hospital.DTO.request;

import com.example.Hospital.model.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(max = 120, message = "Name must not exceed 120 characters")
        String name,

        @Email(message = "Invalid email format")
        @Size(max = 180, message = "Email must not exceed 180 characters")
        String email,

        @Size(max = 20, message = "Phone must not exceed 20 characters")
        String phone,

        UserRole role,

        Long departmentId,

        Boolean active
) {}
