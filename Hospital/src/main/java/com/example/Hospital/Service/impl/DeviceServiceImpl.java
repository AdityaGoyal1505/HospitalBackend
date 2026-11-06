package com.example.Hospital.service.impl;

import com.example.Hospital.dto.DeviceRegisterRequestDTO;
import com.example.Hospital.dto.DeviceResponseDTO;
import com.example.Hospital.model.DeviceToken;
import com.example.Hospital.exception.ResourceNotFoundException;
import com.example.Hospital.repository.DeviceTokenRepository;
import com.example.Hospital.service.DeviceService;
import java.util.List;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private static final Pattern NO_WHITESPACE = Pattern.compile("^\\S+$");

    private final DeviceTokenRepository deviceTokenRepository;

    private DeviceResponseDTO toResponse(DeviceToken device) {
        DeviceResponseDTO dto = new DeviceResponseDTO();
        dto.setId(device.getId());
        dto.setPlatform(device.getPlatform());
        dto.setCreatedAt(device.getCreatedAt());
        dto.setUpdatedAt(device.getUpdatedAt());
        return dto;
    }

    @Override
    @Transactional
    public DeviceResponseDTO register(DeviceRegisterRequestDTO request) {
        String token = request.getToken().trim();
        if (!NO_WHITESPACE.matcher(token).matches()) {
            throw new IllegalArgumentException("Token must not contain spaces");
        }

        DeviceToken device = deviceTokenRepository.findByToken(token).orElseGet(() -> {
            DeviceToken created = new DeviceToken();
            created.setToken(token);
            return created;
        });
        device.setUserId(request.getUserId());
        device.setPlatform(request.getPlatform());

        return toResponse(deviceTokenRepository.save(device));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceResponseDTO> getDevicesByUser(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User id must be positive");
        }
        return deviceTokenRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void unregister(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token is required");
        }
        DeviceToken device = deviceTokenRepository.findByToken(token.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Device token not found"));
        deviceTokenRepository.delete(device);
    }
}

