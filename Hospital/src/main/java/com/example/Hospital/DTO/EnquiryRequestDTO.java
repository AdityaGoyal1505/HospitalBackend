package com.example.Hospital.dto;

import com.example.Hospital.model.enums.EnquiryType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnquiryRequestDTO {


    @Positive(message = "Patient id must be positive")
    private Long patientId;

    @NotBlank(message = "Patient name is required")
    @Size(max = 150, message = "Patient name must be at most 150 characters")
    private String patientName;

    @NotBlank(message = "Patient email is required")
    @Email(message = "Patient email is not valid")
    @Size(max = 150, message = "Patient email must be at most 150 characters")
    private String patientEmail;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$",
            message = "Patient phone must be 10 to 15 digits, with an optional leading +")
    private String patientPhone;

    @Size(max = 100, message = "Country must be at most 100 characters")
    private String country;

    @NotNull(message = "Enquiry type is required")
    private EnquiryType enquiryType;

    @NotBlank(message = "Message is required")
    @Size(max = 2000, message = "Message must be at most 2000 characters")
    private String message;

    @Size(max = 200, message = "Treatment interest must be at most 200 characters")
    private String treatmentInterest;

    @FutureOrPresent(message = "Preferred date cannot be in the past")
    private LocalDate preferredDate;
}
