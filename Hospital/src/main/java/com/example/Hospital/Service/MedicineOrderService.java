package com.example.Hospital.service;

import com.example.Hospital.dto.MedicineOrderRequestDTO;
import com.example.Hospital.dto.MedicineOrderResponseDTO;
import com.example.Hospital.dto.OrderStatusUpdateRequestDTO;
import com.example.Hospital.model.enums.MedicineOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MedicineOrderService {

    MedicineOrderResponseDTO createOrder(MedicineOrderRequestDTO request, String idempotencyKey);

    MedicineOrderResponseDTO updateStatus(Long id, OrderStatusUpdateRequestDTO request);

    MedicineOrderResponseDTO cancelOrder(Long orderId, Long patientId, String reason);

    MedicineOrderResponseDTO getById(Long id);

    Page<MedicineOrderResponseDTO> getOrders(Long patientId, MedicineOrderStatus status, Pageable pageable);
}

