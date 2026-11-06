package com.example.Hospital.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MedicineOrderRequestDTO {

    @NotNull(message = "Patient id is required")
    @Positive(message = "Patient id must be positive")
    private Long patientId;

    @NotEmpty(message = "At least one item is required")
    @Size(max = 50, message = "An order can have at most 50 items")
    @Valid
    private List<OrderItemRequestDTO> items;

    private Long prescriptionDocumentId;

    @Size(max = 500, message = "Prescription file must be at most 500 characters")
    private String prescriptionFile;

    @Size(max = 500, message = "Delivery address must be at most 500 characters")
    private String deliveryAddress;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$",
            message = "Contact phone must be 10 to 15 digits, with an optional leading +")
    private String contactPhone;
}
