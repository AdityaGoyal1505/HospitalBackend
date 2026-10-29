package com.example.Hospital.Service;

import com.example.Hospital.DTO.request.CreateDoctorRequest;
import com.example.Hospital.DTO.request.UpdateDoctorRequest;
import com.example.Hospital.DTO.response.DoctorResponse;
import com.example.Hospital.Repository.DepartmentRepository;
import com.example.Hospital.Repository.DoctorRepository;
import com.example.Hospital.Repository.UserRepository;
import com.example.Hospital.model.Department;
import com.example.Hospital.model.Doctor;
import com.example.Hospital.model.User;
import com.example.Hospital.model.enums.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    public DoctorService(DoctorRepository doctorRepository,
                         UserRepository userRepository,
                         DepartmentRepository departmentRepository) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    @Transactional
    public DoctorResponse createDoctor(CreateDoctorRequest req) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor name is required");
        }

        if (req.getSpecialization() == null || req.getSpecialization().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor specialization is required");
        }

        Doctor doc = new Doctor();
        doc.setName(req.getName().trim());
        doc.setSpecialization(req.getSpecialization().trim());
        doc.setQualification(req.getQualification() != null ? req.getQualification().trim() : null);
        doc.setExperience(req.getExperience());

        if (req.getUserId() != null) {
            User user = userRepository.findById(req.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            if (user.getRole() != UserRole.DOCTOR) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User must have DOCTOR role");
            }

            if (doctorRepository.existsByUser_Id(user.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "A doctor profile already exists for this user");
            }
            doc.setUser(user);
        }

        if (req.getDepartmentId() != null) {
            Department department = departmentRepository.findById(req.getDepartmentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
            doc.setDepartment(department);
        }

        Doctor saved = doctorRepository.save(doc);
        return toResponse(saved);
    }

    // --- Services around ID ---
    @Transactional(readOnly = true)
    public DoctorResponse getDoctorById(Long id) {
        Doctor doc = doctorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found with id: " + id));
        return toResponse(doc);
    }

    @Transactional(readOnly = true)
    public boolean existsDoctorById(Long id) {
        return doctorRepository.existsById(id);
    }

    @Transactional
    public DoctorResponse updateDoctor(Long id, UpdateDoctorRequest req) {
        Doctor doc = doctorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found with id: " + id));

        if (req.getName() != null && !req.getName().isBlank()) {
            doc.setName(req.getName().trim());
        }

        if (req.getSpecialization() != null && !req.getSpecialization().isBlank()) {
            doc.setSpecialization(req.getSpecialization().trim());
        }

        if (req.getQualification() != null) {
            doc.setQualification(req.getQualification().trim());
        }

        if (req.getExperience() != null) {
            doc.setExperience(req.getExperience());
        }

        if (req.getDepartmentId() != null) {
            Department department = departmentRepository.findById(req.getDepartmentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
            doc.setDepartment(department);
        }

        if (req.getUserId() != null) {
            User user = userRepository.findById(req.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
            doc.setUser(user);
        }

        Doctor updated = doctorRepository.save(doc);
        return toResponse(updated);
    }

    @Transactional
    public void deleteDoctor(Long id) {
        Doctor doc = doctorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found with id: " + id));
        doctorRepository.delete(doc);
    }

    // --- Services around Name ---
    @Transactional(readOnly = true)
    public List<DoctorResponse> getDoctorsByName(String name) {
        if (name == null || name.isBlank()) {
            return getAllDoctors();
        }
        return doctorRepository.findByNameContainingIgnoreCase(name.trim()).stream()
                .map(this::toResponse)
                .toList();
    }

    // --- Services around Specialization ---
    @Transactional(readOnly = true)
    public List<DoctorResponse> getDoctorsBySpecialization(String specialization) {
        if (specialization == null || specialization.isBlank()) {
            return getAllDoctors();
        }
        return doctorRepository.findBySpecializationContainingIgnoreCase(specialization.trim()).stream()
                .map(this::toResponse)
                .toList();
    }

    // --- Combined Search Service ---
    @Transactional(readOnly = true)
    public List<DoctorResponse> searchDoctors(String name, String specialization) {
        boolean hasName = name != null && !name.isBlank();
        boolean hasSpec = specialization != null && !specialization.isBlank();

        if (hasName && hasSpec) {
            return doctorRepository
                    .findByNameContainingIgnoreCaseAndSpecializationContainingIgnoreCase(name.trim(), specialization.trim())
                    .stream()
                    .map(this::toResponse)
                    .toList();
        } else if (hasName) {
            return getDoctorsByName(name);
        } else if (hasSpec) {
            return getDoctorsBySpecialization(specialization);
        } else {
            return getAllDoctors();
        }
    }

    @Transactional(readOnly = true)
    public List<DoctorResponse> getAllDoctors() {
        return doctorRepository.findAll().stream().map(this::toResponse).toList();
    }

    private DoctorResponse toResponse(Doctor doc) {
        DoctorResponse response = new DoctorResponse();

        response.setId(doc.getId());
        response.setName(doc.getName());
        response.setSpecialization(doc.getSpecialization());
        response.setQualification(doc.getQualification());
        response.setExperience(doc.getExperience());

        if (doc.getUser() != null) {
            response.setUser_id(doc.getUser().getId());
            response.setUser_name(doc.getUser().getName());
            response.setUser_phone(doc.getUser().getPhone());
            response.setUser_email(doc.getUser().getEmail());
        }

        if (doc.getDepartment() != null) {
            response.setDepartment_id(doc.getDepartment().getId());
            response.setDepartment_name(doc.getDepartment().getName() != null ? doc.getDepartment().getName().name() : null);
        }

        return response;
    }
}
