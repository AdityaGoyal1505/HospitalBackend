package com.example.Hospital.DTO.response;

import com.example.Hospital.model.enums.UserRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponse {

    private String accessToken;
    private String tokenType;
    private long expiresInSeconds;
    private Long userId;
    private String name;
    private UserRole role;
}
