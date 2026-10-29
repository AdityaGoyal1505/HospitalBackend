package com.example.Hospital.Repository;

import com.example.Hospital.model.HospitalService;
import com.example.Hospital.model.enums.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HospitalServiceRepository extends JpaRepository<HospitalService, Long> {

    List<HospitalService> findByActiveTrue();

    List<HospitalService> findByTypeAndActiveTrue(ServiceType type);

    List<HospitalService> findByDepartment_IdAndActiveTrue(Long departmentId);
}