package com.example.Hospital.Controller;

import com.example.Hospital.Config.CurrentUser;
import com.example.Hospital.DTO.DeviceRegisterRequestDTO;
import com.example.Hospital.DTO.DeviceResponseDTO;
import com.example.Hospital.Service.DeviceService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    // The user always comes from the login token, never from the request.

    @PostMapping("/register")
    public ResponseEntity<DeviceResponseDTO> register(
            @Valid @RequestBody DeviceRegisterRequestDTO request) {
        return ResponseEntity.ok(deviceService.register(CurrentUser.id(), request));
    }

    // My own phones.
    @GetMapping
    public ResponseEntity<List<DeviceResponseDTO>> myDevices() {
        return ResponseEntity.ok(deviceService.getDevicesByUser(CurrentUser.id()));
    }

    @DeleteMapping("/token/{token}")
    public ResponseEntity<Void> unregister(@PathVariable String token) {
        deviceService.unregister(CurrentUser.id(), token);
        return ResponseEntity.noContent().build();
    }
}