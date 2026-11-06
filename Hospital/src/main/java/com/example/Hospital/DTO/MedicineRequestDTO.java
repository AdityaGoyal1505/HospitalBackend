package com.example.Hospital.dto;

import com.example.Hospital.model.Medicine;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MedicineRequestDTO {

    @NotBlank(message = "Name is required")
    @Size(max = 150, message = "Name must be at most 150 characters")
    private String medicineName;

    @Size(max = 2000, message = "Description must be at most 2000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price can have at most 8 digits and 2 decimals")
    private BigDecimal price;

    private boolean requiresPrescription;

    private Boolean active;

    @Min(value = 0, message = "Initial quantity cannot be negative")
    @Max(value = Medicine.MAX_QUANTITY, message = "Initial quantity is too large")
    private Integer quantity;
}

