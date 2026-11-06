package com.example.Hospital.Service;

import com.example.Hospital.DTO.request.CreatePackageRequest;
import com.example.Hospital.DTO.request.UpdatePackageRequest;
import com.example.Hospital.DTO.response.PackageResponse;
import com.example.Hospital.Repository.MedicalPackageRepository;
import com.example.Hospital.model.MedicalPackage;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PackageService {

    private final MedicalPackageRepository medicalPackageRepository;

    public PackageService(MedicalPackageRepository medicalPackageRepository) {
        this.medicalPackageRepository = medicalPackageRepository;
    }

    @Transactional
    public PackageResponse createPackage(CreatePackageRequest request) {
        String trimmedName = request.name().trim();
        if (medicalPackageRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A package with name '" + trimmedName + "' already exists");
        }

        MedicalPackage pkg = new MedicalPackage();
        pkg.setName(trimmedName);
        pkg.setDescription(request.description() != null ? request.description().trim() : null);
        pkg.setPrice(request.price());
        pkg.setActive(true);

        MedicalPackage saved = medicalPackageRepository.save(pkg);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PackageResponse> getAllPackages(boolean activeOnly) {
        List<MedicalPackage> packages = activeOnly
                ? medicalPackageRepository.findByActiveTrueOrderByNameAsc()
                : medicalPackageRepository.findAllByOrderByNameAsc();

        return packages.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PackageResponse getPackageById(Long id) {
        MedicalPackage pkg = medicalPackageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Package not found with id " + id));
        return mapToResponse(pkg);
    }

    @Transactional
    public PackageResponse updatePackage(Long id, UpdatePackageRequest request) {
        MedicalPackage pkg = medicalPackageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Package not found with id " + id));

        String trimmedName = request.name().trim();
        if (medicalPackageRepository.existsByNameIgnoreCaseAndIdNot(trimmedName, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Another package with name '" + trimmedName + "' already exists");
        }

        pkg.setName(trimmedName);
        pkg.setDescription(request.description() != null ? request.description().trim() : null);
        pkg.setPrice(request.price());
        if (request.active() != null) {
            pkg.setActive(request.active());
        }

        MedicalPackage updated = medicalPackageRepository.save(pkg);
        return mapToResponse(updated);
    }

    @Transactional
    public PackageResponse updatePackageStatus(Long id, boolean active) {
        MedicalPackage pkg = medicalPackageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Package not found with id " + id));

        pkg.setActive(active);
        MedicalPackage updated = medicalPackageRepository.save(pkg);
        return mapToResponse(updated);
    }

    @Transactional
    public void deletePackage(Long id) {
        if (!medicalPackageRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Package not found with id " + id);
        }
        medicalPackageRepository.deleteById(id);
    }

    public PackageResponse mapToResponse(MedicalPackage pkg) {
        return new PackageResponse(
                pkg.getId(),
                pkg.getName(),
                pkg.getDescription(),
                pkg.getPrice(),
                pkg.isActive(),
                pkg.getCreatedAt(),
                pkg.getUpdatedAt()
        );
    }
}
