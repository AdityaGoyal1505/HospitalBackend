package com.example.Hospital.service;

import com.example.Hospital.dto.MedicineRequestDTO;
import com.example.Hospital.dto.MedicineResponseDTO;
import com.example.Hospital.dto.StockUpdateRequestDTO;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MedicineService {

    MedicineResponseDTO addMedicine(MedicineRequestDTO request);

    MedicineResponseDTO getById(Long id);

    MedicineResponseDTO getByIdForStaff(Long id);

    Page<MedicineResponseDTO> getAllActive(String medicineName, Pageable pageable);

    MedicineResponseDTO update(Long id, MedicineRequestDTO request);

    MedicineResponseDTO updatePrice(Long id, BigDecimal price);

    void deactivate(Long id);

    void activate(Long id);

    void delete(Long id);

    Page<MedicineResponseDTO> getAllForStaff(Pageable pageable);

    MedicineResponseDTO updateStock(Long id, StockUpdateRequestDTO request);

    Page<MedicineResponseDTO> getLowStockMedicines(Pageable pageable);
}
