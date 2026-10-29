package com.example.Hospital.Controller;

import com.example.Hospital.DTO.request.CreateDepartmentRequest;
import com.example.Hospital.DTO.request.UpdateDepartmentRequest;
import com.example.Hospital.DTO.response.DepartmentResponse;
import com.example.Hospital.Service.DepartmentService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public DepartmentResponse createDepartment(@Valid @RequestBody CreateDepartmentRequest request) {
        return departmentService.createDepartment(request);
    }

    @GetMapping
    public List<DepartmentResponse> getAllDepartments(@RequestParam(defaultValue = "false") boolean activeOnly) {
        return departmentService.getAllDepartments(activeOnly);
    }

    @GetMapping("/{id}")
    public DepartmentResponse getDepartmentById(@PathVariable Long id) {
        return departmentService.getDepartmentById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public DepartmentResponse updateDepartment(@PathVariable Long id, @Valid @RequestBody UpdateDepartmentRequest request) {
        return departmentService.updateDepartment(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public DepartmentResponse updateDepartmentStatus(@PathVariable Long id, @RequestParam boolean active) {
        return departmentService.updateDepartmentStatus(id, active);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
    }
}
