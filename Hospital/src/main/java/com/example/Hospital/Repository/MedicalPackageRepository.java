package com.example.Hospital.Repository;

import com.example.Hospital.model.MedicalPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalPackageRepository extends JpaRepository<MedicalPackage, Long> {

    Optional<MedicalPackage> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    List<MedicalPackage> findByActiveTrueOrderByNameAsc();

    List<MedicalPackage> findAllByOrderByNameAsc();
}
