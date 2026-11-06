package com.example.Hospital;

import com.example.Hospital.DTO.request.CreatePackageRequest;
import com.example.Hospital.DTO.request.UpdatePackageRequest;
import com.example.Hospital.DTO.response.PackageResponse;
import com.example.Hospital.Repository.MedicalPackageRepository;
import com.example.Hospital.Service.PackageService;
import com.example.Hospital.model.MedicalPackage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class PackageServiceTest {

    @Mock
    private MedicalPackageRepository medicalPackageRepository;

    @InjectMocks
    private PackageService packageService;

    private MedicalPackage testPackage;

    @BeforeEach
    void setUp() {
        testPackage = new MedicalPackage();
        testPackage.setId(1L);
        testPackage.setName("Comprehensive Full Body Checkup");
        testPackage.setDescription("Complete blood panel and physician consultation");
        testPackage.setPrice(new BigDecimal("2499.00"));
        testPackage.setActive(true);
        testPackage.setCreatedAt(Instant.now());
        testPackage.setUpdatedAt(Instant.now());
    }

    @Test
    void createPackage_Success() {
        CreatePackageRequest request = new CreatePackageRequest(
                "Comprehensive Full Body Checkup",
                "Complete blood panel and physician consultation",
                new BigDecimal("2499.00")
        );

        when(medicalPackageRepository.existsByNameIgnoreCase("Comprehensive Full Body Checkup")).thenReturn(false);
        when(medicalPackageRepository.save(any(MedicalPackage.class))).thenReturn(testPackage);

        PackageResponse response = packageService.createPackage(request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Comprehensive Full Body Checkup");
        assertThat(response.price()).isEqualTo(new BigDecimal("2499.00"));
        assertThat(response.active()).isTrue();
        verify(medicalPackageRepository).save(any(MedicalPackage.class));
    }

    @Test
    void createPackage_DuplicateName_ThrowsConflict() {
        CreatePackageRequest request = new CreatePackageRequest(
                "Comprehensive Full Body Checkup",
                "Description",
                new BigDecimal("2499.00")
        );

        when(medicalPackageRepository.existsByNameIgnoreCase("Comprehensive Full Body Checkup")).thenReturn(true);

        assertThatThrownBy(() -> packageService.createPackage(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("already exists");

        verify(medicalPackageRepository, never()).save(any(MedicalPackage.class));
    }

    @Test
    void getAllPackages_ActiveOnlyTrue() {
        when(medicalPackageRepository.findByActiveTrueOrderByNameAsc()).thenReturn(List.of(testPackage));

        List<PackageResponse> list = packageService.getAllPackages(true);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).name()).isEqualTo("Comprehensive Full Body Checkup");
        verify(medicalPackageRepository).findByActiveTrueOrderByNameAsc();
    }

    @Test
    void getPackageById_Found() {
        when(medicalPackageRepository.findById(1L)).thenReturn(Optional.of(testPackage));

        PackageResponse response = packageService.getPackageById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Comprehensive Full Body Checkup");
    }

    @Test
    void getPackageById_NotFound_Throws404() {
        when(medicalPackageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> packageService.getPackageById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void updatePackage_Success() {
        UpdatePackageRequest request = new UpdatePackageRequest(
                "Updated Package Name",
                "New description",
                new BigDecimal("2999.00"),
                true
        );

        when(medicalPackageRepository.findById(1L)).thenReturn(Optional.of(testPackage));
        when(medicalPackageRepository.existsByNameIgnoreCaseAndIdNot("Updated Package Name", 1L)).thenReturn(false);
        when(medicalPackageRepository.save(any(MedicalPackage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PackageResponse response = packageService.updatePackage(1L, request);

        assertThat(response.name()).isEqualTo("Updated Package Name");
        assertThat(response.price()).isEqualTo(new BigDecimal("2999.00"));
    }

    @Test
    void updatePackageStatus_Success() {
        when(medicalPackageRepository.findById(1L)).thenReturn(Optional.of(testPackage));
        when(medicalPackageRepository.save(any(MedicalPackage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PackageResponse response = packageService.updatePackageStatus(1L, false);

        assertThat(response.active()).isFalse();
    }

    @Test
    void deletePackage_Success() {
        when(medicalPackageRepository.existsById(1L)).thenReturn(true);

        packageService.deletePackage(1L);

        verify(medicalPackageRepository).deleteById(1L);
    }
}
