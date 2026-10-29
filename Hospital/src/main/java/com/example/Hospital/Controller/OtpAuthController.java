package com.example.Hospital.Controller;

import com.example.Hospital.DTO.request.RequestOtpRequest;
import com.example.Hospital.DTO.request.SignupRequest;
import com.example.Hospital.DTO.request.VerifyOtpRequest;
import com.example.Hospital.DTO.request.VerifySignupOtpRequest;
import com.example.Hospital.DTO.response.AuthResponse;
import com.example.Hospital.Service.OtpAuthenticationService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/otp")
public class OtpAuthController {

    private final OtpAuthenticationService otpService;

    public OtpAuthController(OtpAuthenticationService otpService) {
        this.otpService = otpService;
    }

    @PostMapping("/check-phone")
    public ResponseEntity<Map<String, Object>> checkPhone(@Valid @RequestBody RequestOtpRequest request) {
        boolean isExisting = otpService.isPhoneRegistered(request.getPhone());
        return ResponseEntity.ok(Map.of(
                "phone", request.getPhone(),
                "isExistingUser", isExisting,
                "action", isExisting ? "LOGIN" : "SIGNUP",
                "message", isExisting
                        ? "User exists. Proceed to request login OTP."
                        : "User does not exist. Proceed to request signup OTP."
        ));
    }

    @PostMapping("/signup/request")
    public ResponseEntity<Map<String, String>> requestSignupOtp(@Valid @RequestBody SignupRequest request) {
        otpService.requestSignupOtp(request);
        return ResponseEntity.ok(Map.of("message", "If signup is available for this number, an OTP has been sent."));
    }

    @PostMapping("/signup/verify")
    public ResponseEntity<AuthResponse> verifySignupOtp(@Valid @RequestBody VerifySignupOtpRequest request) {
        return ResponseEntity.ok(otpService.verifySignupOtp(request));
    }

    @PostMapping("/login/request")
    public ResponseEntity<Map<String, String>> requestLoginOtp(@Valid @RequestBody RequestOtpRequest request) {
        otpService.requestLoginOtp(request);
        return ResponseEntity.ok(Map.of("message", "If login is available for this number, an OTP has been sent."));
    }

    @PostMapping("/login/verify")
    public ResponseEntity<AuthResponse> verifyLoginOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(otpService.verifyLoginOtp(request));
    }
}
