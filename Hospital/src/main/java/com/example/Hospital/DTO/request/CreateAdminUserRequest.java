
package com.example.Hospital.DTO.request;

import com.example.Hospital.model.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAdminUserRequest(

        @NotBlank
        @Size(max = 120)
        String name,

        @Email
        @Size(max = 180)
        String email,

        @NotBlank
        @Size(max = 20)
        String phone,

        @NotNull
        UserRole role,

        Long departmentId
) {
}
