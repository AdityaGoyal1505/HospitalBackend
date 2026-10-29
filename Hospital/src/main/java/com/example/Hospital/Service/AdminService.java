package com.example.Hospital.Service;

import com.example.Hospital.DTO.request.CreateAdminUserRequest;
import com.example.Hospital.DTO.request.UpdateUserRequest;
import com.example.Hospital.DTO.response.AdminUserResponse;
import com.example.Hospital.Repository.DepartmentRepository;
import com.example.Hospital.Repository.UserRepository;
import com.example.Hospital.model.Department;
import com.example.Hospital.model.User;
import com.example.Hospital.model.enums.UserRole;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    public AdminService(UserRepository userRepository, DepartmentRepository departmentRepository) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    @Transactional
    public AdminUserResponse createUser(CreateAdminUserRequest request) {
        UserRole role = request.role();

        if (role == UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin accounts cannot be created through this endpoint");
        }

        String name = request.name().trim();
        String phone = request.phone().trim();
        String email = request.email() == null || request.email().isBlank() ? null : request.email().trim().toLowerCase();

        if (userRepository.existsByPhone(phone)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone number is already registered");
        }

        if (email != null && userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }

        Department department = null;
        if (request.departmentId() != null) {
            department = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
        }

        if (role != UserRole.DOCTOR && department != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A department can only be assigned to a doctor");
        }

        User user = new User();
        user.setName(name);
        user.setPhone(phone);
        user.setEmail(email);
        user.setRole(role);
        user.setDepartment(department);
        user.setActive(true);
        user.setProfileCompleted(false);

        User savedUser = userRepository.save(user);
        return toResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AdminUserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return toResponse(user);
    }

    @Transactional
    public AdminUserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name().trim());
        }

        if (request.email() != null) {
            String email = request.email().trim().toLowerCase();
            if (!email.isBlank()) {
                boolean inUse = userRepository.findByEmailIgnoreCase(email)
                        .filter(e -> !e.getId().equals(user.getId()))
                        .isPresent();
                if (inUse) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
                }
                user.setEmail(email);
            }
        }

        if (request.phone() != null && !request.phone().isBlank()) {
            String phone = request.phone().trim();
            boolean inUse = userRepository.findByPhone(phone)
                    .filter(e -> !e.getId().equals(user.getId()))
                    .isPresent();
            if (inUse) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone is already registered");
            }
            user.setPhone(phone);
        }

        if (request.role() != null) {
            user.setRole(request.role());
        }

        if (request.departmentId() != null) {
            Department department = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
            user.setDepartment(department);
        }

        if (request.active() != null) {
            user.setActive(request.active());
        }

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public AdminUserResponse updateUserStatus(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setActive(active);
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        userRepository.delete(user);
    }

    private AdminUserResponse toResponse(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().name(),
                user.getDepartment() == null ? null : user.getDepartment().getId(),
                user.isActive(),
                user.isProfileCompleted(),
                user.getCreatedAt()
        );
    }
}
