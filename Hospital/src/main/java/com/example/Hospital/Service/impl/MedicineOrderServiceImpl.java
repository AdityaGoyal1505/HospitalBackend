package com.example.Hospital.service.impl;

import com.example.Hospital.dto.MedicineOrderRequestDTO;
import com.example.Hospital.dto.MedicineOrderResponseDTO;
import com.example.Hospital.dto.OrderStatusUpdateRequestDTO;
import com.example.Hospital.model.MedicineOrder;
import com.example.Hospital.model.enums.MedicineOrderStatus;
import com.example.Hospital.exception.ResourceNotFoundException;
import com.example.Hospital.repository.MedicineOrderRepository;
import com.example.Hospital.service.MedicineOrderService;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicineOrderServiceImpl implements MedicineOrderService {

    private static final int MAX_ATTEMPTS = 3;
    private static final int MAX_KEY_LENGTH = 64;

    private final MedicineOrderProcessor processor;
    private final MedicineOrderRepository orderRepository;

    private MedicineOrderResponseDTO toResponse(MedicineOrder order) {
        MedicineOrderResponseDTO dto = new MedicineOrderResponseDTO();
        dto.setId(order.getId());
        dto.setOrderReference(order.getOrderReference());
        dto.setPatientId(order.getPatientId());
        dto.setItems(order.getItems());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setPrescriptionFile(order.getPrescriptionFile());
        dto.setDeliveryAddress(order.getDeliveryAddress());
        dto.setContactPhone(order.getContactPhone());
        dto.setStatus(order.getStatus());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        return dto;
    }

    private MedicineOrder withRetry(Supplier<MedicineOrder> attempt) {
        for (int tryNumber = 1; ; tryNumber++) {
            try {
                return attempt.get();
            } catch (OptimisticLockingFailureException e) {
                if (tryNumber >= MAX_ATTEMPTS) {
                    log.warn("Giving up after {} attempts because of version conflicts", tryNumber);
                    throw e;
                }
                log.warn("Version conflict, retrying (attempt {} of {})", tryNumber, MAX_ATTEMPTS);
            }
        }
    }

    private String normalizeKey(String rawKey) {
        if (rawKey == null || rawKey.isBlank()) {
            return null;
        }
        String key = rawKey.trim();
        if (key.length() > MAX_KEY_LENGTH) {
            throw new IllegalArgumentException(
                    "Idempotency-Key must be at most " + MAX_KEY_LENGTH + " characters");
        }
        return key;
    }

    @Override
    public MedicineOrderResponseDTO createOrder(MedicineOrderRequestDTO request, String idempotencyKey) {
        String key = normalizeKey(idempotencyKey);

        if (key != null) {
            Optional<MedicineOrder> existing =
                    orderRepository.findByPatientIdAndIdempotencyKey(request.getPatientId(), key);
            if (existing.isPresent()) {
                return toResponse(existing.get());
            }
        }

        try {
            return toResponse(withRetry(() -> processor.create(request, key)));
        } catch (DataIntegrityViolationException e) {
            if (key != null) {
                return orderRepository.findByPatientIdAndIdempotencyKey(request.getPatientId(), key)
                        .map(this::toResponse)
                        .orElseThrow(() -> e);
            }
            throw e;
        }
    }

    @Override
    public MedicineOrderResponseDTO updateStatus(Long id, OrderStatusUpdateRequestDTO request) {
        return toResponse(withRetry(
                () -> processor.changeStatus(id, request.getStatus(), request.getStaffNotes())));
    }

    @Override
    public MedicineOrderResponseDTO cancelOrder(Long orderId, Long patientId, String reason) {
        return toResponse(withRetry(
                () -> processor.cancelByPatient(orderId, patientId, reason)));
    }

    @Override
    @Transactional(readOnly = true)
    public MedicineOrderResponseDTO getById(Long id) {
        MedicineOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + id));
        return toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MedicineOrderResponseDTO> getOrders(Long patientId, MedicineOrderStatus status, Pageable pageable) {
        Page<MedicineOrder> orders;
        if (patientId != null && status != null) {
            orders = orderRepository.findByPatientIdAndStatus(patientId, status, pageable);
        } else if (patientId != null) {
            orders = orderRepository.findByPatientId(patientId, pageable);
        } else if (status != null) {
            orders = orderRepository.findByStatus(status, pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }
        return orders.map(this::toResponse);
    }
}

