package com.example.Hospital.service;

import com.example.Hospital.dto.DeviceRegisterRequestDTO;
import com.example.Hospital.dto.DeviceResponseDTO;
import java.util.List;

public interface DeviceService {

    DeviceResponseDTO register(DeviceRegisterRequestDTO request);

    List<DeviceResponseDTO> getDevicesByUser(Long userId);

    void unregister(String token);
}
