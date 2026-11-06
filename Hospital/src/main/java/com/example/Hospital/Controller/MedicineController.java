package com.example.Hospital.controller;

import com.example.Hospital.dto.MedicineRequestDTO;
import com.example.Hospital.dto.MedicineResponseDTO;
import com.example.Hospital.dto.StockUpdateRequestDTO;
import com.example.Hospital.service.MedicineService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/medicines")
@RequiredArgsConstructor
public class MedicineController {

    private final MedicineService medicineService;

    @PostMapping
    public ResponseEntity<MedicineResponseDTO> addMedicine(@Valid @RequestBody MedicineRequestDTO request) {
        MedicineResponseDTO created = medicineService.addMedicine(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<Page<MedicineResponseDTO>> getAllActive(
            @RequestParam(name = "name", required = false) String medicineName,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(medicineService.getAllActive(medicineName, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicineResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(medicineService.getById(id));
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<MedicineResponseDTO> getByIdForStaff(@PathVariable Long id) {
        return ResponseEntity.ok(medicineService.getByIdForStaff(id));
    }

    @GetMapping("/admin")
    public ResponseEntity<Page<MedicineResponseDTO>> getAllForStaff(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(medicineService.getAllForStaff(pageable));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<Page<MedicineResponseDTO>> getLowStock(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(medicineService.getLowStockMedicines(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicineResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody MedicineRequestDTO request) {
        return ResponseEntity.ok(medicineService.update(id, request));
    }

    @PatchMapping("/{id}/price")
    public ResponseEntity<MedicineResponseDTO> updatePrice(
            @PathVariable Long id,
            @RequestParam BigDecimal price) {
        return ResponseEntity.ok(medicineService.updatePrice(id, price));
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<MedicineResponseDTO> updateStock(
            @PathVariable Long id,
            @Valid @RequestBody StockUpdateRequestDTO request) {
        return ResponseEntity.ok(medicineService.updateStock(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        medicineService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long id) {
        medicineService.activate(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        medicineService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
