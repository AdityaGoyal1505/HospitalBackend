
package com.example.Hospital.Controller;

import com.example.Hospital.DTO.request.CompleteProfileRequest;
import com.example.Hospital.DTO.request.UpdateProfileRequest;
import com.example.Hospital.DTO.response.UserResponse;
import com.example.Hospital.Service.UserProfileService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/me")
    public UserResponse getMyProfile(Authentication authentication) {

        Long userId = getAuthenticatedUserId(authentication);

        return userProfileService.getMyProfile(userId);
    }

    @PutMapping("/me")
    public UserResponse updateMyProfile(Authentication authentication, @Valid @RequestBody UpdateProfileRequest request) {

        Long userId = getAuthenticatedUserId(authentication);

        return userProfileService.updateMyProfile(userId, request);
    }

    @PutMapping("/me/complete")
    public UserResponse completeUserProfile(Authentication auth,@Valid @RequestBody CompleteProfileRequest req){
        Long userId=getAuthenticatedUserId(auth);
        return userProfileService.completeTheProfile(userId,req);
    }
    private Long getAuthenticatedUserId(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication principal");
        }

        return Long.valueOf(jwt.getSubject());
    }
}
