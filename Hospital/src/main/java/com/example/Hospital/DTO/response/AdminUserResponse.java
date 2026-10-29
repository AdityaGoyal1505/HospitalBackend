
package com.example.Hospital.DTO.response;

import java.time.Instant;

public record AdminUserResponse(
        Long id,
        String name,
        String email,
        String phone,
        String role,
        Long departmentId,
        boolean active,
        boolean profileCompleted,
        Instant createdAt
) {
}
