package com.example.Hospital.service.impl;

import static com.example.Hospital.model.enums.MedicineOrderStatus.*;

import com.example.Hospital.dto.MedicineOrderRequestDTO;
import com.example.Hospital.dto.OrderItemRequestDTO;
import com.example.Hospital.model.Medicine;
import com.example.Hospital.model.MedicineOrder;
import com.example.Hospital.model.OrderItem;
import com.example.Hospital.model.PatientDocument;
import com.example.Hospital.model.enums.DocumentType;
import com.example.Hospital.model.enums.MedicineOrderStatus;
import com.example.Hospital.model.enums.NotificationType;
import com.example.Hospital.exception.InvalidOrderStateException;
import com.example.Hospital.exception.ResourceNotFoundException;
import com.example.Hospital.repository.MedicineOrderRepository;
import com.example.Hospital.repository.MedicineRepository;
import com.example.Hospital.repository.PatientDocumentRepository;
import com.example.Hospital.service.NotificationService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MedicineOrderProcessor {

    private static final BigDecimal MAX_TOTAL = new BigDecimal("99999999.99");
    private static final ZoneId HOSPITAL_ZONE = ZoneId.of("Asia/Kolkata");

    private static final Map<MedicineOrderStatus, Set<MedicineOrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            PENDING, Set.of(UNDER_REVIEW, ACCEPTED, REJECTED, CANCELLED),
            UNDER_REVIEW, Set.of(ACCEPTED, REJECTED, CANCELLED),
            ACCEPTED, Set.of(PREPARING, CANCELLED),
            PREPARING, Set.of(READY, CANCELLED),
            READY, Set.of(COMPLETED),
            COMPLETED, Set.of(),
            REJECTED, Set.of(),
            CANCELLED, Set.of());

    private final MedicineRepository medicineRepository;
    private final MedicineOrderRepository orderRepository;
    private final NotificationService notificationService;
    private final PatientDocumentRepository patientDocumentRepository;

    @Transactional
    public MedicineOrder create(MedicineOrderRequestDTO request, String idempotencyKey) {

        Map<Long, Integer> wanted = new LinkedHashMap<>();
        for (OrderItemRequestDTO item : request.getItems()) {
            wanted.merge(item.getMedicineId(), item.getQuantity(), Integer::sum);
        }

        Map<Long, Medicine> medicines = medicineRepository.findAllById(wanted.keySet()).stream()
                .collect(Collectors.toMap(Medicine::getMedicineId, Function.identity()));

        boolean anyRequiresPrescription = false;
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : wanted.entrySet()) {
            Medicine medicine = medicines.get(entry.getKey());
            if (medicine == null || !medicine.isActive()) {
                throw new ResourceNotFoundException("Medicine not found with id " + entry.getKey());
            }
            if (medicine.isRequiresPrescription()) {
                anyRequiresPrescription = true;
            }

            int quantity = entry.getValue();
            medicine.decreaseStock(quantity); // throws InsufficientStockException

            orderItems.add(new OrderItem(
                    medicine.getMedicineId(), medicine.getMedicineName(), quantity, medicine.getPrice()));
            total = total.add(medicine.getPrice().multiply(BigDecimal.valueOf(quantity)));
        }

        String verifiedPrescription = request.getPrescriptionFile();
        if (request.getPrescriptionDocumentId() != null) {
            PatientDocument doc = patientDocumentRepository.findById(request.getPrescriptionDocumentId())
                    .orElseThrow(() -> new IllegalArgumentException("Prescription document not found"));
            if (!doc.getPatientId().equals(request.getPatientId())) {
                throw new IllegalArgumentException("Prescription document does not belong to this patient");
            }
            if (doc.getDocumentType() != DocumentType.PRESCRIPTION) {
                throw new IllegalArgumentException("Document is not of type PRESCRIPTION");
            }
            verifiedPrescription = doc.getFilePath();
        } else if (anyRequiresPrescription && isBlank(verifiedPrescription)) {
            throw new IllegalArgumentException("A valid prescription is required for prescription-only medicines");
        }

        if (total.compareTo(MAX_TOTAL) > 0) {
            throw new IllegalArgumentException("Order total is too large");
        }

        medicineRepository.saveAll(medicines.values());

        MedicineOrder order = new MedicineOrder();
        order.setIdempotencyKey(idempotencyKey);
        order.setPatientId(request.getPatientId());
        order.setItems(orderItems);
        order.setTotalAmount(total);
        order.setPrescriptionFile(verifiedPrescription);
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setContactPhone(request.getContactPhone());
        order.setStatus(PENDING);

        String reference = String.format("ORD-%d-%s",
                LocalDate.now(HOSPITAL_ZONE).getYear(),
                UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setOrderReference(reference);

        MedicineOrder saved = orderRepository.save(order);

        notificationService.create(saved.getPatientId(), "Order placed",
                "Your order " + saved.getOrderReference() + " has been placed.",
                NotificationType.ORDER);
        return saved;
    }

    @Transactional
    public MedicineOrder changeStatus(Long orderId, MedicineOrderStatus newStatus, String staffNotes) {
        MedicineOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + orderId));

        Set<MedicineOrderStatus> allowed = ALLOWED_TRANSITIONS.get(order.getStatus());
        if (allowed == null || !allowed.contains(newStatus)) {
            throw new InvalidOrderStateException(
                    "Cannot change order from " + order.getStatus() + " to " + newStatus);
        }

        if (newStatus == REJECTED || newStatus == CANCELLED) {
            restoreStock(order);
        }

        order.setStatus(newStatus);
        if (!isBlank(staffNotes)) {
            order.setStaffNotes(staffNotes);
        }
        MedicineOrder saved = orderRepository.save(order);

        notificationService.create(saved.getPatientId(), "Order update",
                orderMessage(saved.getOrderReference(), newStatus), NotificationType.ORDER);
        return saved;
    }

    @Transactional
    public MedicineOrder cancelByPatient(Long orderId, Long patientId, String reason) {
        MedicineOrder order = orderRepository.findByIdAndPatientId(orderId, patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found for id " + orderId));

        if (order.getStatus() != PENDING && order.getStatus() != UNDER_REVIEW) {
            throw new InvalidOrderStateException(
                    "Cannot cancel order once processing has begun. Current status is " + order.getStatus());
        }

        restoreStock(order);
        order.setStatus(CANCELLED);
        String note = "Cancelled by patient" + (isBlank(reason) ? "" : ": " + reason.trim());
        order.setStaffNotes(note);
        MedicineOrder saved = orderRepository.save(order);

        notificationService.create(saved.getPatientId(), "Order cancelled",
                "Your order " + saved.getOrderReference() + " has been cancelled.",
                NotificationType.ORDER);
        return saved;
    }

    private void restoreStock(MedicineOrder order) {
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (OrderItem item : order.getItems()) {
            quantities.merge(item.getMedicineId(), item.getQuantity(), Integer::sum);
        }

        List<Medicine> medicines = medicineRepository.findAllById(quantities.keySet());
        if (medicines.size() != quantities.size()) {
            log.warn("Order {} has medicines that no longer exist; their stock was not restored", order.getId());
        }
        for (Medicine medicine : medicines) {
            int toRestore = quantities.get(medicine.getMedicineId());
            int capacity = Medicine.MAX_QUANTITY - medicine.getQuantity();
            if (capacity > 0) {
                medicine.increaseStock(Math.min(toRestore, capacity));
            }
        }
        medicineRepository.saveAll(medicines);
    }

    private static String orderMessage(String reference, MedicineOrderStatus status) {
        String ref = (reference == null) ? "" : " " + reference;
        return switch (status) {
            case PENDING -> "Your order" + ref + " has been placed.";
            case UNDER_REVIEW -> "Your order" + ref + " is being reviewed.";
            case ACCEPTED -> "Your order" + ref + " has been accepted.";
            case PREPARING -> "Your order" + ref + " is being prepared.";
            case READY -> "Your order" + ref + " is ready.";
            case COMPLETED -> "Your order" + ref + " is completed.";
            case REJECTED -> "Your order" + ref + " could not be accepted. Please contact the hospital.";
            case CANCELLED -> "Your order" + ref + " has been cancelled.";
        };
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

