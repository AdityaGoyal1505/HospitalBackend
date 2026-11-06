package com.example.Hospital.controller;

import com.example.Hospital.dto.MedicineOrderRequestDTO;
import com.example.Hospital.dto.MedicineOrderResponseDTO;
import com.example.Hospital.dto.OrderStatusUpdateRequestDTO;
import com.example.Hospital.model.enums.MedicineOrderStatus;
import com.example.Hospital.service.MedicineOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/medicine-orders")
@RequiredArgsConstructor
public class MedicineOrderController {

    private final MedicineOrderService orderService;

    @PostMapping
    public ResponseEntity<MedicineOrderResponseDTO> createOrder(
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody MedicineOrderRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(request, idempotencyKey));
    }

    @GetMapping
    public ResponseEntity<Page<MedicineOrderResponseDTO>> getOrders(
            @RequestParam(name = "patientId", required = false) Long patientId,
            @RequestParam(name = "status", required = false) MedicineOrderStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrders(patientId, status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicineOrderResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getById(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<MedicineOrderResponseDTO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequestDTO request) {
        return ResponseEntity.ok(orderService.updateStatus(id, request));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<MedicineOrderResponseDTO> cancelOrder(
            @PathVariable Long id,
            @RequestParam("patientId") Long patientId,
            @RequestParam(name = "reason", required = false) String reason) {
        return ResponseEntity.ok(orderService.cancelOrder(id, patientId, reason));
    }
}

