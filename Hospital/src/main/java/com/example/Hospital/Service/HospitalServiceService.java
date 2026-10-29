package com.example.Hospital.Service;

import com.example.Hospital.model.HospitalService;
import com.example.Hospital.DTO.request.CreateHospitalServiceRequest;
import com.example.Hospital.DTO.request.UpdateHospitalServiceRequest;
import com.example.Hospital.DTO.response.HospitalServiceResponse;
import com.example.Hospital.Repository.DepartmentRepository;
import com.example.Hospital.Repository.HospitalServiceRepository;
import com.example.Hospital.model.Department;
import com.example.Hospital.model.enums.ServiceType;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class HospitalServiceService {

    private final HospitalServiceRepository hospitalServiceRepository;
    private final DepartmentRepository departmentRepository;

    public HospitalServiceService(HospitalServiceRepository hospitalServiceRepository, DepartmentRepository departmentRepository) {
        this.hospitalServiceRepository = hospitalServiceRepository;
        this.departmentRepository = departmentRepository;
    }

    @Transactional
    public HospitalServiceResponse createService(CreateHospitalServiceRequest request) {
        HospitalService service = new HospitalService();

        service.setName(request.name().trim());
        service.setType(request.type());
        service.setDescription(request.description());
        service.setPrice(request.price());
        service.setDepartment(resolveDepartment(request.departmentId()));
        service.setActive(true);

        return toResponse(hospitalServiceRepository.save(service));
    }

    @Transactional(readOnly = true)
    public List<HospitalServiceResponse> getServices(ServiceType type, Long departmentId, boolean activeOnly) {
        List<HospitalService> baseList = activeOnly
                ? hospitalServiceRepository.findByActiveTrue()
                : hospitalServiceRepository.findAll();

        return baseList.stream()
                .filter(service -> type == null || service.getType() == type)
                .filter(service -> departmentId == null || (service.getDepartment() != null && service.getDepartment().getId().equals(departmentId)))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public HospitalServiceResponse getServiceById(Long id) {
        HospitalService service = findService(id);
        return toResponse(service);
    }

    @Transactional
    public HospitalServiceResponse updateService(Long id, UpdateHospitalServiceRequest request) {
        HospitalService service = findService(id);
        service.setName(request.name().trim());
        service.setType(request.type());
        service.setDescription(request.description());
        service.setPrice(request.price());
        service.setDepartment(resolveDepartment(request.departmentId()));

        return toResponse(hospitalServiceRepository.save(service));
    }

    @Transactional
    public HospitalServiceResponse updateServiceStatus(Long id, boolean active) {
        HospitalService service = findService(id);
        service.setActive(active);

        return toResponse(hospitalServiceRepository.save(service));
    }

    @Transactional
    public void deleteService(Long id) {
        HospitalService service = findService(id);
        hospitalServiceRepository.delete(service);
    }

    private HospitalService findService(Long id) {
        return hospitalServiceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));
    }

    private Department resolveDepartment(Long departmentId) {
        if (departmentId == null) {
            return null;
        }

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));

        if (!department.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot assign a service to an inactive department");
        }

        return department;
    }

    private HospitalServiceResponse toResponse(HospitalService service) {
        Department department = service.getDepartment();

        return new HospitalServiceResponse(
                service.getId(),
                service.getName(),
                service.getType(),
                service.getDescription(),
                service.getPrice(),
                department != null ? department.getId() : null,
                department != null ? String.valueOf(department.getName()) : null,
                service.isActive()
        );
    }
}
