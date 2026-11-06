package com.example.Hospital.dto;

import com.example.Hospital.model.enums.MedicineOrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderStatusUpdateRequestDTO {

    @NotNull(message = "Status is required")
    private MedicineOrderStatus status;

    @Size(max = 2000, message = "Staff notes must be at most 2000 characters")
    private String staffNotes;
}
