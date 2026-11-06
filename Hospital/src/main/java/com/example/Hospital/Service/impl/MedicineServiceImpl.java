package com.example.Hospital.service.impl;

import com.example.Hospital.dto.MedicineRequestDTO;
import com.example.Hospital.dto.MedicineResponseDTO;
import com.example.Hospital.dto.StockUpdateRequestDTO;
import com.example.Hospital.model.Medicine;
import com.example.Hospital.exception.ResourceNotFoundException;
import com.example.Hospital.repository.MedicineRepository;
import com.example.Hospital.service.MedicineService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MedicineServiceImpl implements MedicineService {

    private static final BigDecimal MAX_PRICE = new BigDecimal("99999999.99");

    private final MedicineRepository medicineRepository;

    @Value("${hospital.stock.low-threshold:5}")
    private int lowStockThreshold;

    private MedicineResponseDTO toResponse(Medicine medicine) {
        MedicineResponseDTO dto = new MedicineResponseDTO();
        dto.setMedicineId(medicine.getMedicineId());
        dto.setMedicineName(medicine.getMedicineName());
        dto.setDescription(medicine.getDescription());
        dto.setPrice(medicine.getPrice());
        dto.setRequiresPrescription(medicine.isRequiresPrescription());
        dto.setActive(medicine.isActive());
        dto.setQuantity(medicine.getQuantity());
        dto.setCreatedAt(medicine.getCreatedAt());
        dto.setUpdatedAt(medicine.getUpdatedAt());
        return dto;
    }

    private void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }
        if (price.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("Price can have at most 2 decimal places");
        }
        if (price.compareTo(MAX_PRICE) > 0) {
            throw new IllegalArgumentException("Price cannot exceed " + MAX_PRICE.toPlainString());
        }
    }

    @Override
    @Transactional
    public MedicineResponseDTO addMedicine(MedicineRequestDTO request) {
        String cleanName = request.getMedicineName().trim();
        if (medicineRepository.existsByMedicineNameIgnoreCaseAndActiveTrue(cleanName)) {
            throw new IllegalArgumentException("An active medicine with this name already exists");
        }
        validatePrice(request.getPrice());

        Medicine medicine = new Medicine();
        medicine.setMedicineName(cleanName);
        medicine.setDescription(request.getDescription());
        medicine.setPrice(request.getPrice());
        medicine.setRequiresPrescription(request.isRequiresPrescription());
        if (request.getActive() != null) {
            medicine.setActive(request.getActive());
        }
        if (request.getQuantity() != null) {
            medicine.updateStock(request.getQuantity());
        }
        Medicine saved = medicineRepository.save(medicine);
        return toResponse(saved);
    }

    @Override
    public MedicineResponseDTO getById(Long medicineId) {
        Medicine medicine = medicineRepository.findById(medicineId)
                .filter(Medicine::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id " + medicineId));
        return toResponse(medicine);
    }

    @Override
    public MedicineResponseDTO getByIdForStaff(Long medicineId) {
        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id " + medicineId));
        return toResponse(medicine);
    }

    @Override
    public Page<MedicineResponseDTO> getAllActive(String medicineName, Pageable pageable) {
        Page<Medicine> result;
        if (medicineName == null || medicineName.isBlank()) {
            result = medicineRepository.findByActiveTrue(pageable);
        } else {
            result = medicineRepository.findByActiveTrueAndMedicineNameContainingIgnoreCase(medicineName.trim(), pageable);
        }
        return result.map(this::toResponse);
    }

    @Override
    @Transactional
    public MedicineResponseDTO update(Long id, MedicineRequestDTO request) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id " + id));

        String cleanName = request.getMedicineName().trim();
        if (medicineRepository.existsByMedicineNameIgnoreCaseAndMedicineIdNotAndActiveTrue(cleanName, id)) {
            throw new IllegalArgumentException("Another active medicine with this name already exists");
        }
        validatePrice(request.getPrice());

        medicine.setMedicineName(cleanName);
        medicine.setDescription(request.getDescription());
        medicine.setPrice(request.getPrice());
        medicine.setRequiresPrescription(request.isRequiresPrescription());
        if (request.getActive() != null) {
            medicine.setActive(request.getActive());
        }
        if (request.getQuantity() != null) {
            medicine.updateStock(request.getQuantity());
        }
        Medicine saved = medicineRepository.save(medicine);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public MedicineResponseDTO updatePrice(Long id, BigDecimal price) {
        validatePrice(price);
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id " + id));
        medicine.setPrice(price);
        Medicine saved = medicineRepository.save(medicine);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id " + id));
        medicine.setActive(false);
        medicineRepository.save(medicine);
    }

    @Override
    @Transactional
    public void activate(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id " + id));
        medicine.setActive(true);
        medicineRepository.save(medicine);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id " + id));
        medicineRepository.delete(medicine);
    }

    @Override
    public Page<MedicineResponseDTO> getAllForStaff(Pageable pageable) {
        return medicineRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public MedicineResponseDTO updateStock(Long id, StockUpdateRequestDTO request) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id " + id));
        medicine.updateStock(request.getQuantity());
        Medicine saved = medicineRepository.save(medicine);
        return toResponse(saved);
    }

    @Override
    public Page<MedicineResponseDTO> getLowStockMedicines(Pageable pageable) {
        return medicineRepository
                .findByActiveTrueAndQuantityLessThan(lowStockThreshold, pageable)
                .map(this::toResponse);
    }
}

