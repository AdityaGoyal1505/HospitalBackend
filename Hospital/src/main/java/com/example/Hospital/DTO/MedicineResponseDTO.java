package com.example.Hospital.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
public class MedicineResponseDTO {
    private Long medicineId;
    private String medicineName;
    private String description;
    private BigDecimal price;
    private boolean requiresPrescription;
    private Boolean active;
    private int quantity;
    private Instant createdAt;
    private Instant updatedAt;
}