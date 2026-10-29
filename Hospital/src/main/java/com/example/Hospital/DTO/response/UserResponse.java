
package com.example.Hospital.DTO.response;

import com.example.Hospital.model.enums.UserRole;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private UserRole role;

    private Long departmentId;
    private String departmentName;

    private boolean profileCompleted;
    private boolean active;
    private Instant createdAt;
}
