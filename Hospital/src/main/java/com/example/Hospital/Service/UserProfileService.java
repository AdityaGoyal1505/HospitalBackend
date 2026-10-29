package com.example.Hospital.Service;

import com.example.Hospital.DTO.request.CompleteProfileRequest;
import com.example.Hospital.DTO.request.UpdateProfileRequest;
import com.example.Hospital.DTO.response.UserResponse;
import com.example.Hospital.Repository.UserRepository;
import com.example.Hospital.model.User;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserProfileService {

    private final UserRepository userRepository;

    public UserProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserResponse getMyProfile(Long userId) {
        User user = getActiveUser(userId);
        return mapToResponse(user);
    }

    @Transactional
    public UserResponse completeTheProfile(Long userId, CompleteProfileRequest req) {
        User user = getActiveUser(userId);

        String name = req.name() != null ? req.name().trim() : user.getName();
        String email = req.email() != null && !req.email().isBlank() ? req.email().trim().toLowerCase() : null;
        String phone = req.phone() != null && !req.phone().isBlank() ? req.phone().trim() : user.getPhone();

        if (email != null) {
            boolean emailInUse = userRepository.findByEmailIgnoreCase(email)
                    .filter(existing -> !existing.getId().equals(user.getId()))
                    .isPresent();

            if (emailInUse) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
            }
        }

        if (phone != null && !phone.equals(user.getPhone())) {
            boolean phoneInUse = userRepository.findByPhone(phone)
                    .filter(existing -> !existing.getId().equals(user.getId()))
                    .isPresent();

            if (phoneInUse) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone number is already registered");
            }
            user.setPhone(phone);
        }

        user.setName(name);
        user.setEmail(email);
        user.setProfileCompleted(true);

        User saved = userRepository.save(user);
        return mapToResponse(saved);
    }

    @Transactional
    public UserResponse updateMyProfile(Long userId, UpdateProfileRequest request) {
        User user = getActiveUser(userId);

        String name = request.getName().trim();
        String email = request.getEmail();

        if (email != null && !email.isBlank()) {
            email = email.trim().toLowerCase();

            boolean emailBelongsToAnotherUser =
                    userRepository.findByEmailIgnoreCase(email)
                            .filter(existing -> !existing.getId().equals(user.getId()))
                            .isPresent();

            if (emailBelongsToAnotherUser) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use");
            }
        } else {
            email = null;
        }

        user.setName(name);
        user.setEmail(email);

        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    private User getActiveUser(Long userId) {
        return userRepository.findById(userId)
                .filter(User::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Active user not found"));
    }

    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole());
        response.setProfileCompleted(user.isProfileCompleted());
        response.setActive(user.isActive());
        response.setCreatedAt(user.getCreatedAt());

        if (user.getDepartment() != null) {
            response.setDepartmentId(user.getDepartment().getId());
            response.setDepartmentName(user.getDepartment().getName().name());
        }

        return response;
    }
}
