package com.example.Hospital;

import com.example.Hospital.DTO.request.CreatePackageEnquiryRequest;
import com.example.Hospital.DTO.request.UpdatePackageEnquiryStatusRequest;
import com.example.Hospital.DTO.response.PackageEnquiryResponse;
import com.example.Hospital.Repository.MedicalPackageRepository;
import com.example.Hospital.Repository.PackageEnquiryRepository;
import com.example.Hospital.Repository.UserRepository;
import com.example.Hospital.Service.PackageEnquiryService;
import com.example.Hospital.model.MedicalPackage;
import com.example.Hospital.model.PackageEnquiry;
import com.example.Hospital.model.User;
import com.example.Hospital.model.enums.PackageEnquiryStatus;
import com.example.Hospital.model.enums.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PackageEnquiryServiceTest {

    @Mock
    private PackageEnquiryRepository enquiryRepository;

    @Mock
    private MedicalPackageRepository medicalPackageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PackageEnquiryService enquiryService;

    private User testUser;
    private MedicalPackage testPackage;
    private PackageEnquiry testEnquiry;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(5L);
        testUser.setName("Alice Sharma");
        testUser.setPhone("+919876543210");
        testUser.setEmail("alice@example.com");
        testUser.setRole(UserRole.PATIENT);
        testUser.setActive(true);

        testPackage = new MedicalPackage();
        testPackage.setId(10L);
        testPackage.setName("Comprehensive Full Body Checkup");
        testPackage.setPrice(new BigDecimal("2499.00"));

        testEnquiry = new PackageEnquiry();
        testEnquiry.setId(1L);
        testEnquiry.setUser(testUser);
        testEnquiry.setName("Alice Sharma");
        testEnquiry.setPhone("+919876543210");
        testEnquiry.setEmail("alice@example.com");
        testEnquiry.setPackageName("Comprehensive Full Body Checkup");
        testEnquiry.setMedicalPackage(testPackage);
        testEnquiry.setMessage("Interested in weekend slot");
        testEnquiry.setStatus(PackageEnquiryStatus.PENDING);
        testEnquiry.setCreatedAt(Instant.now());
        testEnquiry.setUpdatedAt(Instant.now());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockAuthenticatedUser(Long userId) {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(String.valueOf(userId));

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(jwt);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createEnquiry_withValidExplicitData_shouldSucceed() {
        mockAuthenticatedUser(5L);
        when(userRepository.findById(5L)).thenReturn(Optional.of(testUser));
        when(medicalPackageRepository.findById(10L)).thenReturn(Optional.of(testPackage));
        when(enquiryRepository.save(any(PackageEnquiry.class))).thenAnswer(invocation -> {
            PackageEnquiry e = invocation.getArgument(0);
            e.setId(1L);
            e.setCreatedAt(Instant.now());
            e.setUpdatedAt(Instant.now());
            return e;
        });

        CreatePackageEnquiryRequest request = new CreatePackageEnquiryRequest(
                "Custom Name",
                "+919123456789",
                "custom@example.com",
                "Comprehensive Full Body Checkup",
                10L,
                "Please call in the morning"
        );

        PackageEnquiryResponse response = enquiryService.createEnquiry(request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Custom Name");
        assertThat(response.phone()).isEqualTo("+919123456789");
        assertThat(response.email()).isEqualTo("custom@example.com");
        assertThat(response.packageName()).isEqualTo("Comprehensive Full Body Checkup");
        assertThat(response.packageId()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo(PackageEnquiryStatus.PENDING);
        assertThat(response.message()).isEqualTo("Please call in the morning");

        verify(enquiryRepository).save(any(PackageEnquiry.class));
    }

    @Test
    void createEnquiry_withOmittedProfileFields_shouldDefaultToUserAccount() {
        mockAuthenticatedUser(5L);
        when(userRepository.findById(5L)).thenReturn(Optional.of(testUser));
        when(medicalPackageRepository.findByNameIgnoreCase("Comprehensive Full Body Checkup"))
                .thenReturn(Optional.of(testPackage));
        when(enquiryRepository.save(any(PackageEnquiry.class))).thenAnswer(invocation -> {
            PackageEnquiry e = invocation.getArgument(0);
            e.setId(2L);
            return e;
        });

        CreatePackageEnquiryRequest request = new CreatePackageEnquiryRequest(
                null,
                null,
                null,
                "Comprehensive Full Body Checkup",
                null,
                null
        );

        PackageEnquiryResponse response = enquiryService.createEnquiry(request);

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Alice Sharma");
        assertThat(response.phone()).isEqualTo("+919876543210");
        assertThat(response.email()).isEqualTo("alice@example.com");
    }

    @Test
    void createEnquiry_withoutAuthentication_shouldThrowUnauthorized() {
        SecurityContextHolder.clearContext();

        CreatePackageEnquiryRequest request = new CreatePackageEnquiryRequest(
                "Alice", "+919876543210", "alice@example.com", "Package X", null, null
        );

        assertThatThrownBy(() -> enquiryService.createEnquiry(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Authentication is required");
    }

    @Test
    void getMyEnquiries_shouldReturnUserSpecificEnquiries() {
        mockAuthenticatedUser(5L);
        when(userRepository.findById(5L)).thenReturn(Optional.of(testUser));
        when(enquiryRepository.findByUserIdOrderByCreatedAtDesc(5L)).thenReturn(List.of(testEnquiry));

        List<PackageEnquiryResponse> result = enquiryService.getMyEnquiries();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Alice Sharma");
        assertThat(result.get(0).packageName()).isEqualTo("Comprehensive Full Body Checkup");
    }

    @Test
    void getAllEnquiries_withStatusFilter_shouldFilterByStatus() {
        when(enquiryRepository.findByStatusOrderByCreatedAtDesc(PackageEnquiryStatus.PENDING))
                .thenReturn(List.of(testEnquiry));

        List<PackageEnquiryResponse> result = enquiryService.getAllEnquiries(PackageEnquiryStatus.PENDING);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isEqualTo(PackageEnquiryStatus.PENDING);
        verify(enquiryRepository).findByStatusOrderByCreatedAtDesc(PackageEnquiryStatus.PENDING);
    }

    @Test
    void getAllEnquiries_withoutStatusFilter_shouldReturnAll() {
        when(enquiryRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(testEnquiry));

        List<PackageEnquiryResponse> result = enquiryService.getAllEnquiries(null);

        assertThat(result).hasSize(1);
        verify(enquiryRepository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void getEnquiryById_asOwner_shouldSucceed() {
        mockAuthenticatedUser(5L);
        when(userRepository.findById(5L)).thenReturn(Optional.of(testUser));
        when(enquiryRepository.findById(1L)).thenReturn(Optional.of(testEnquiry));

        PackageEnquiryResponse response = enquiryService.getEnquiryById(1L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    void getEnquiryById_asUnrelatedPatient_shouldThrowForbidden() {
        User otherPatient = new User();
        otherPatient.setId(99L);
        otherPatient.setRole(UserRole.PATIENT);

        mockAuthenticatedUser(99L);
        when(userRepository.findById(99L)).thenReturn(Optional.of(otherPatient));
        when(enquiryRepository.findById(1L)).thenReturn(Optional.of(testEnquiry));

        assertThatThrownBy(() -> enquiryService.getEnquiryById(1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Access denied to this enquiry record");
    }

    @Test
    void updateEnquiryStatus_asStaff_shouldUpdateStatusAndContactNotes() {
        User receptionist = new User();
        receptionist.setId(2L);
        receptionist.setName("Bob Reception");
        receptionist.setRole(UserRole.RECEPTION);

        mockAuthenticatedUser(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(receptionist));
        when(enquiryRepository.findById(1L)).thenReturn(Optional.of(testEnquiry));
        when(enquiryRepository.save(any(PackageEnquiry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdatePackageEnquiryStatusRequest updateRequest = new UpdatePackageEnquiryStatusRequest(
                PackageEnquiryStatus.CONTACTED,
                "Called patient, confirmed visit on Friday"
        );

        PackageEnquiryResponse response = enquiryService.updateEnquiryStatus(1L, updateRequest);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(PackageEnquiryStatus.CONTACTED);
        assertThat(response.contactNotes()).isEqualTo("Called patient, confirmed visit on Friday");
        assertThat(response.contactedByUserId()).isEqualTo(2L);
        assertThat(response.contactedByUserName()).isEqualTo("Bob Reception");
        assertThat(response.contactedAt()).isNotNull();
    }

    @Test
    void deleteEnquiry_whenExists_shouldDeleteSuccessfully() {
        when(enquiryRepository.existsById(1L)).thenReturn(true);
        doNothing().when(enquiryRepository).deleteById(1L);

        enquiryService.deleteEnquiry(1L);

        verify(enquiryRepository).deleteById(1L);
    }
}
