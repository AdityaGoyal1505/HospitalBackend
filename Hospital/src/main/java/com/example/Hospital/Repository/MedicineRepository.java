package com.example.Hospital.repository;

import com.example.Hospital.model.Medicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    Page<Medicine> findByActiveTrue(Pageable pageable);

    Page<Medicine> findByActiveTrueAndMedicineNameContainingIgnoreCase(String medicineName, Pageable pageable);

    Page<Medicine> findByActiveTrueAndQuantityLessThan(int threshold, Pageable pageable);

    boolean existsByMedicineNameIgnoreCaseAndActiveTrue(String medicineName);

    boolean existsByMedicineNameIgnoreCaseAndMedicineIdNotAndActiveTrue(String medicineName, Long medicineId);
}

