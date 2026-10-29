package com.example.Hospital.Repository;

import com.example.Hospital.model.Department;
import com.example.Hospital.model.enums.DepartmentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    boolean existsByName(DepartmentType name);

    Optional<Department> findByName(DepartmentType name);
}