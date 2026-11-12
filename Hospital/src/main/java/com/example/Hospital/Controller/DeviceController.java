package com.example.Hospital.controller;

import com.example.Hospital.dto.DeviceRegisterRequestDTO;
import com.example.Hospital.dto.DeviceResponseDTO;
import com.example.Hospital.service.DeviceService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping("/register")
    public ResponseEntity<DeviceResponseDTO> register(
            @Valid @RequestBody DeviceRegisterRequestDTO request) {
        return ResponseEntity.ok(deviceService.register(request));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DeviceResponseDTO>> getDevicesByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(deviceService.getDevicesByUser(userId));
    }

    @DeleteMapping("/token/{token}")
    public ResponseEntity<Void> unregister(@PathVariable String token) {
        deviceService.unregister(token);
        return ResponseEntity.noContent().build();
    }
}
