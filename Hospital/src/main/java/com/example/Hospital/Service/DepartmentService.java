package com.example.Hospital.Service;

import com.example.Hospital.DTO.request.CreateDepartmentRequest;
import com.example.Hospital.DTO.request.UpdateDepartmentRequest;
import com.example.Hospital.DTO.response.DepartmentResponse;
import com.example.Hospital.Repository.DepartmentRepository;
import com.example.Hospital.model.Department;
import com.example.Hospital.model.enums.DepartmentType;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Transactional
    public DepartmentResponse createDepartment(CreateDepartmentRequest request) {

        DepartmentType name = request.name();

        if (departmentRepository.existsByName(name)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A department with this name already exists"
            );
        }

        Department department = new Department();
        department.setName(name);
        department.setDescription(request.description());
        department.setImageUrl(request.imageUrl());
        department.setActive(true);

        return toResponse(departmentRepository.save(department));
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments(boolean activeOnly) {

        return departmentRepository.findAll()
                .stream()
                .filter(department ->
                        !activeOnly || department.isActive())
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {

        Department department = findDepartment(id);
        return toResponse(department);
    }

    @Transactional
    public DepartmentResponse updateDepartment(
            Long id,
            UpdateDepartmentRequest request) {

        Department department = findDepartment(id);
        DepartmentType name = request.name();

        departmentRepository.findByName(name)
                .filter(existing ->
                        !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "A department with this name already exists"
                    );
                });

        department.setName(name);
        department.setDescription(request.description());
        department.setImageUrl(request.imageUrl());

        return toResponse(departmentRepository.save(department));
    }

    @Transactional
    public DepartmentResponse updateDepartmentStatus(
            Long id,
            boolean active) {

        Department department = findDepartment(id);
        department.setActive(active);

        return toResponse(departmentRepository.save(department));
    }

    @Transactional
    public void deleteDepartment(Long id) {
        Department department = findDepartment(id);
        departmentRepository.delete(department);
    }

    private Department findDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Department not found"
                ));
    }

    private DepartmentResponse toResponse(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getDescription(),
                department.getImageUrl(),
                department.isActive()
        );
    }
}
