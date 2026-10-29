package com.example.Hospital.Repository;

import com.example.Hospital.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    List<Doctor> findByNameContainingIgnoreCase(String name);

    List<Doctor> findBySpecializationIgnoreCase(String specialization);

    List<Doctor> findBySpecializationContainingIgnoreCase(String specialization);

    List<Doctor> findByNameContainingIgnoreCaseAndSpecializationContainingIgnoreCase(String name, String specialization);

    Optional<Doctor> findByUser_Id(Long userId);

    boolean existsByUser_Id(Long userId);
}
