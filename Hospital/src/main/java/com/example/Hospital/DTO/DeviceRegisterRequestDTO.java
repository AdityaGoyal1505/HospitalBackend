package com.example.Hospital.dto;

import com.example.Hospital.model.enums.DevicePlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceRegisterRequestDTO {


    @NotNull(message = "User id is required")
    @Positive(message = "User id must be positive")
    private Long userId;

    @NotBlank(message = "Token is required")
    @Size(max = 512, message = "Token must be at most 512 characters")
    private String token;

    @NotNull(message = "Platform is required (ANDROID or IOS)")
    private DevicePlatform platform;
}
