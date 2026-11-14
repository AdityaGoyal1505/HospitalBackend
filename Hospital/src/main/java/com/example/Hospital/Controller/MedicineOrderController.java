package com.example.Hospital.Controller;

import com.example.Hospital.Config.CurrentUser;
import com.example.Hospital.DTO.MedicineOrderRequestDTO;
import com.example.Hospital.DTO.MedicineOrderResponseDTO;
import com.example.Hospital.DTO.OrderStatusUpdateRequestDTO;
import com.example.Hospital.Service.MedicineOrderService;
import com.example.Hospital.exception.ResourceNotFoundException;
import com.example.Hospital.model.enums.MedicineOrderStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/medicine-orders")
@RequiredArgsConstructor
public class MedicineOrderController {

    private final MedicineOrderService orderService;

    // Patients place orders, always for themselves.
    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<MedicineOrderResponseDTO> createOrder(
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody MedicineOrderRequestDTO request) {
        request.setPatientId(CurrentUser.id()); // whatever the app sent is replaced
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(request, idempotencyKey));
    }

    // Staff can see any patient's orders (or all). Everyone else sees only their own.
    @GetMapping
    public ResponseEntity<Page<MedicineOrderResponseDTO>> getOrders(
            @RequestParam(name = "patientId", required = false) Long patientId,
            @RequestParam(name = "status", required = false) MedicineOrderStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Long patientFilter = CurrentUser.isStaff() ? patientId : CurrentUser.id();
        return ResponseEntity.ok(orderService.getOrders(patientFilter, status, pageable));
    }

    // Staff see any order. A patient sees only their own; someone else's looks like "not found".
    @GetMapping("/{id}")
    public ResponseEntity<MedicineOrderResponseDTO> getById(@PathVariable Long id) {
        MedicineOrderResponseDTO order = orderService.getById(id);
        if (!CurrentUser.isStaff() && !CurrentUser.id().equals(order.getPatientId())) {
            throw new ResourceNotFoundException("Order not found with id " + id);
        }
        return ResponseEntity.ok(order);
    }

    // Only staff move an order through its steps.
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public ResponseEntity<MedicineOrderResponseDTO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequestDTO request) {
        return ResponseEntity.ok(orderService.updateStatus(id, request));
    }

    // A patient cancels their own order (the service allows it only while PENDING or UNDER_REVIEW).
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<MedicineOrderResponseDTO> cancelOrder(
            @PathVariable Long id,
            @RequestParam(name = "reason", required = false) String reason) {
        return ResponseEntity.ok(orderService.cancelOrder(id, CurrentUser.id(), reason));
    }
}