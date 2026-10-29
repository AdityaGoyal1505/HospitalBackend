package com.example.Hospital.DTO.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CompleteProfileRequest(

        @NotBlank(message = "Name is required")
        @Size(max = 120)
        String name,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "Use international phone format")
        String phone,

        @Email(message = "Invalid email format")
        @Size(max = 180)
        String email

) {}
