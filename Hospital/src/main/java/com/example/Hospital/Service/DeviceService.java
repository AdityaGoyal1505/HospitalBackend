package com.example.Hospital.Service;

import com.example.Hospital.DTO.DeviceRegisterRequestDTO;
import com.example.Hospital.DTO.DeviceResponseDTO;
import java.util.List;

public interface DeviceService {

    DeviceResponseDTO register(Long userId, DeviceRegisterRequestDTO request);

    List<DeviceResponseDTO> getDevicesByUser(Long userId);

    void unregister(Long userId, String token);
}