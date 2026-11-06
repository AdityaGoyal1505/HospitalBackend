package com.example.Hospital.repository;

import com.example.Hospital.model.MedicineOrder;
import com.example.Hospital.model.enums.MedicineOrderStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicineOrderRepository extends JpaRepository<MedicineOrder, Long> {

    Page<MedicineOrder> findByPatientId(Long patientId, Pageable pageable);

    Page<MedicineOrder> findByStatus(MedicineOrderStatus status, Pageable pageable);

    Page<MedicineOrder> findByPatientIdAndStatus(Long patientId, MedicineOrderStatus status, Pageable pageable);

    Optional<MedicineOrder> findByIdAndPatientId(Long id, Long patientId);

    Optional<MedicineOrder> findByPatientIdAndIdempotencyKey(Long patientId, String idempotencyKey);
}

