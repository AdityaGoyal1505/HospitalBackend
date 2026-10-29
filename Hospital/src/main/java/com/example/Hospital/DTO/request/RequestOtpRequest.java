package com.example.Hospital.DTO.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestOtpRequest {

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^\\+[1-9]\\d{7,14}$",
            message = "Phone must be in international E.164 format"
    )
    private String phone;
}
