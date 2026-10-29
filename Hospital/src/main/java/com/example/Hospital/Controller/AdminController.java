package com.example.Hospital.Controller;

import com.example.Hospital.DTO.request.CreateAdminUserRequest;
import com.example.Hospital.DTO.request.UpdateUserRequest;
import com.example.Hospital.DTO.response.AdminUserResponse;
import com.example.Hospital.Service.AdminService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminUserResponse createUser(@Valid @RequestBody CreateAdminUserRequest request) {
        return adminService.createUser(request);
    }

    @GetMapping
    public List<AdminUserResponse> getAllUsers() {
        return adminService.getAllUsers();
    }

    @GetMapping("/{id}")
    public AdminUserResponse getUserById(@PathVariable Long id) {
        return adminService.getUserById(id);
    }

    @PutMapping("/{id}")
    public AdminUserResponse updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return adminService.updateUser(id, request);
    }

    @PatchMapping("/{id}/status")
    public AdminUserResponse updateUserStatus(@PathVariable Long id, @RequestParam boolean active) {
        return adminService.updateUserStatus(id, active);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
    }
}
