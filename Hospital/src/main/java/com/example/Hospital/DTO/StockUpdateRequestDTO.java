package com.example.Hospital.dto;


import com.example.Hospital.model.Medicine;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockUpdateRequestDTO {

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    @Max(value = Medicine.MAX_QUANTITY, message = "Quantity is too large")
    private Integer quantity;
}
