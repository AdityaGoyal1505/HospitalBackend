package com.example.Hospital.dto;

import com.example.Hospital.model.enums.DevicePlatform;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceResponseDTO {
    private Long id;
    private DevicePlatform platform;
    private Instant createdAt;
    private Instant updatedAt;
}
