package com.example.Hospital.Service.impl;

import com.example.Hospital.DTO.DeviceRegisterRequestDTO;
import com.example.Hospital.DTO.DeviceResponseDTO;
import com.example.Hospital.Repository.DeviceTokenRepository;
import com.example.Hospital.Service.DeviceService;
import com.example.Hospital.exception.ResourceNotFoundException;
import com.example.Hospital.model.DeviceToken;
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

    private static void requirePositive(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User id must be positive");
        }
    }

    @Override
    @Transactional
    public DeviceResponseDTO register(Long userId, DeviceRegisterRequestDTO request) {
        requirePositive(userId);

        String token = request.getToken().trim();
        if (!NO_WHITESPACE.matcher(token).matches()) {
            throw new IllegalArgumentException("Token must not contain spaces");
        }

        // A phone address is saved once. If it already exists, it is updated and moved to
        // the user who registered it now, so the previous user stops receiving its messages.
        DeviceToken device = deviceTokenRepository.findByToken(token).orElseGet(() -> {
            DeviceToken created = new DeviceToken();
            created.setToken(token);
            return created;
        });
        device.setUserId(userId);
        device.setPlatform(request.getPlatform());


        return toResponse(deviceTokenRepository.save(device));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceResponseDTO> getDevicesByUser(Long userId) {
        requirePositive(userId);
        return deviceTokenRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void unregister(Long userId, String token) {
        requirePositive(userId);
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token is required");
        }

        // Only the owner can remove a phone. Someone else's phone looks like "not found".
        DeviceToken device = deviceTokenRepository.findByToken(token.trim())
                .filter(d -> userId.equals(d.getUserId()))
                .orElseThrow(() -> new ResourceNotFoundException("Device token not found"));
        deviceTokenRepository.delete(device);
    }
}