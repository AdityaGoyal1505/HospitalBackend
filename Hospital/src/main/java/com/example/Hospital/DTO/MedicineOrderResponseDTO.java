package com.example.Hospital.dto;

import com.example.Hospital.model.OrderItem;
import com.example.Hospital.model.enums.MedicineOrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MedicineOrderResponseDTO {
    private Long id;
    private String orderReference;
    private Long patientId;
    private List<OrderItem> items;
    private BigDecimal totalAmount;
    private String prescriptionFile;
    private String deliveryAddress;
    private String contactPhone;
    private MedicineOrderStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
