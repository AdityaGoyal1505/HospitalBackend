package com.example.Hospital.dto;


import com.example.Hospital.model.Medicine;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemRequestDTO {

    @NotNull(message = "Medicine id is required")
    private Long medicineId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = Medicine.MAX_QUANTITY, message = "Quantity is too large")
    private Integer quantity;
}
