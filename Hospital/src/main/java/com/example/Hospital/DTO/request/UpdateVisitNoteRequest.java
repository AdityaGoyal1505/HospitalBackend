package com.example.Hospital.DTO.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateVisitNoteRequest {

    @NotBlank(message = "Visit notes cannot be empty")
    @Size(max = 50000, message = "Visit notes cannot exceed 50000 characters")
    private String notes;
}
