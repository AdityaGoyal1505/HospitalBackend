package com.example.Hospital.Service;

import com.example.Hospital.DTO.request.CreatePackageEnquiryRequest;
import com.example.Hospital.DTO.request.UpdatePackageEnquiryStatusRequest;
import com.example.Hospital.DTO.response.PackageEnquiryResponse;
import com.example.Hospital.Repository.MedicalPackageRepository;
import com.example.Hospital.Repository.PackageEnquiryRepository;
import com.example.Hospital.Repository.UserRepository;
import com.example.Hospital.model.MedicalPackage;
import com.example.Hospital.model.PackageEnquiry;
import com.example.Hospital.model.User;
import com.example.Hospital.model.enums.PackageEnquiryStatus;
import com.example.Hospital.model.enums.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class PackageEnquiryService {

    private final PackageEnquiryRepository enquiryRepository;
    private final MedicalPackageRepository medicalPackageRepository;
    private final UserRepository userRepository;

    public PackageEnquiryService(PackageEnquiryRepository enquiryRepository,
                                 MedicalPackageRepository medicalPackageRepository,
                                 UserRepository userRepository) {
        this.enquiryRepository = enquiryRepository;
        this.medicalPackageRepository = medicalPackageRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public PackageEnquiryResponse createEnquiry(CreatePackageEnquiryRequest request) {
        User currentUser = getAuthenticatedUser();

        String resolvedName = (request.name() != null && !request.name().isBlank())
                ? request.name().trim()
                : currentUser.getName();

        if (resolvedName == null || resolvedName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        }

        String resolvedPhone = (request.phone() != null && !request.phone().isBlank())
                ? request.phone().trim()
                : currentUser.getPhone();

        if (resolvedPhone == null || resolvedPhone.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone number is required");
        }

        String resolvedEmail = (request.email() != null && !request.email().isBlank())
                ? request.email().trim().toLowerCase()
                : (currentUser.getEmail() != null ? currentUser.getEmail().trim().toLowerCase() : null);

        if (resolvedEmail == null || resolvedEmail.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email address is required");
        }

        String trimmedPackageName = request.packageName().trim();

        PackageEnquiry enquiry = new PackageEnquiry();
        enquiry.setUser(currentUser);
        enquiry.setName(resolvedName);
        enquiry.setPhone(resolvedPhone);
        enquiry.setEmail(resolvedEmail);
        enquiry.setPackageName(trimmedPackageName);
        enquiry.setMessage(request.message() != null ? request.message().trim() : null);
        enquiry.setStatus(PackageEnquiryStatus.PENDING);

        // Resolve package linkage if packageId is supplied or match by name
        if (request.packageId() != null) {
            medicalPackageRepository.findById(request.packageId())
                    .ifPresent(pkg -> {
                        enquiry.setMedicalPackage(pkg);
                        enquiry.setPackageName(pkg.getName());
                    });
        } else {
            medicalPackageRepository.findByNameIgnoreCase(trimmedPackageName)
                    .ifPresent(enquiry::setMedicalPackage);
        }

        PackageEnquiry saved = enquiryRepository.save(enquiry);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PackageEnquiryResponse> getMyEnquiries() {
        User currentUser = getAuthenticatedUser();
        List<PackageEnquiry> list = enquiryRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
        return list.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PackageEnquiryResponse> getAllEnquiries(PackageEnquiryStatus status) {
        List<PackageEnquiry> enquiries = (status != null)
                ? enquiryRepository.findByStatusOrderByCreatedAtDesc(status)
                : enquiryRepository.findAllByOrderByCreatedAtDesc();

        return enquiries.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PackageEnquiryResponse getEnquiryById(Long id) {
        User currentUser = getAuthenticatedUser();
        PackageEnquiry enquiry = enquiryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Package enquiry not found with id " + id));

        // Allow if user is staff (ADMIN, RECEPTION) or the owner of the enquiry
        boolean isStaff = currentUser.getRole() == UserRole.ADMIN || currentUser.getRole() == UserRole.RECEPTION;
        boolean isOwner = enquiry.getUser() != null && enquiry.getUser().getId().equals(currentUser.getId());

        if (!isStaff && !isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this enquiry record");
        }

        return mapToResponse(enquiry);
    }

    @Transactional
    public PackageEnquiryResponse updateEnquiryStatus(Long id, UpdatePackageEnquiryStatusRequest request) {
        PackageEnquiry enquiry = enquiryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Package enquiry not found with id " + id));

        enquiry.setStatus(request.status());
        if (request.contactNotes() != null) {
            enquiry.setContactNotes(request.contactNotes().trim());
        }

        User currentUser = getAuthenticatedUser();
        enquiry.setContactedBy(currentUser);
        enquiry.setContactedAt(Instant.now());

        PackageEnquiry updated = enquiryRepository.save(enquiry);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteEnquiry(Long id) {
        if (!enquiryRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Package enquiry not found with id " + id);
        }
        enquiryRepository.deleteById(id);
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        try {
            Long userId = Long.valueOf(jwt.getSubject());
            return userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid user identifier in token");
        }
    }

    public PackageEnquiryResponse mapToResponse(PackageEnquiry enquiry) {
        Long userId = enquiry.getUser() != null ? enquiry.getUser().getId() : null;
        Long packageId = enquiry.getMedicalPackage() != null ? enquiry.getMedicalPackage().getId() : null;
        Long contactedByUserId = enquiry.getContactedBy() != null ? enquiry.getContactedBy().getId() : null;
        String contactedByUserName = enquiry.getContactedBy() != null ? enquiry.getContactedBy().getName() : null;

        return new PackageEnquiryResponse(
                enquiry.getId(),
                userId,
                enquiry.getName(),
                enquiry.getPhone(),
                enquiry.getEmail(),
                enquiry.getPackageName(),
                packageId,
                enquiry.getMessage(),
                enquiry.getStatus(),
                enquiry.getContactNotes(),
                contactedByUserId,
                contactedByUserName,
                enquiry.getContactedAt(),
                enquiry.getCreatedAt(),
                enquiry.getUpdatedAt()
        );
    }
}
