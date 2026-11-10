package com.example.Hospital;

import com.example.Hospital.DTO.request.CreateDoctorRequest;
import com.example.Hospital.DTO.request.UpdateDoctorRequest;
import com.example.Hospital.DTO.response.DoctorResponse;
import com.example.Hospital.Repository.DepartmentRepository;
import com.example.Hospital.Repository.DoctorRepository;
import com.example.Hospital.Repository.UserRepository;
import com.example.Hospital.Service.DoctorService;
import com.example.Hospital.model.Department;
import com.example.Hospital.model.Doctor;
import com.example.Hospital.model.User;
import com.example.Hospital.model.enums.DepartmentType;
import com.example.Hospital.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DoctorService doctorService;

    private Doctor sampleDoctor;
    private Department sampleDepartment;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleDepartment = new Department();
        sampleDepartment.setId(10L);
        sampleDepartment.setName(DepartmentType.ORTHOPAEDICS);

        sampleUser = new User();
        sampleUser.setId(20L);
        sampleUser.setName("Dr. Sarah Jenkins");
        sampleUser.setPhone("+919876543210");
        sampleUser.setEmail("sarah.jenkins@hospital.com");
        sampleUser.setRole(UserRole.DOCTOR);

        sampleDoctor = new Doctor("Dr. Sarah Jenkins", "Cardiology", "MBBS, MD", 12, "https://hospital.com/images/doctors/sarah.jpg");
        sampleDoctor.setId(1L);
        sampleDoctor.setDepartment(sampleDepartment);
        sampleDoctor.setUser(sampleUser);
    }

    @Test
    void testDoctorModel_gettersAndSetters() {
        Doctor doc = new Doctor();
        doc.setImageUrl("https://hospital.com/images/doctors/doc1.png");
        assertThat(doc.getImageUrl()).isEqualTo("https://hospital.com/images/doctors/doc1.png");
        assertThat(doc.getImage_url()).isEqualTo("https://hospital.com/images/doctors/doc1.png");

        doc.setImage_url("https://hospital.com/images/doctors/doc2.png");
        assertThat(doc.getImageUrl()).isEqualTo("https://hospital.com/images/doctors/doc2.png");
        assertThat(doc.getImage_url()).isEqualTo("https://hospital.com/images/doctors/doc2.png");

        Doctor docWithConstructor = new Doctor("Dr. John", "Neurology", "MBBS", 5, "https://hospital.com/images/john.jpg");
        assertThat(docWithConstructor.getImageUrl()).isEqualTo("https://hospital.com/images/john.jpg");
    }

    @Test
    void testCreateDoctor_withImageUrl_success() {
        CreateDoctorRequest req = new CreateDoctorRequest();
        req.setName("Dr. John Doe");
        req.setSpecialization("Orthopedics");
        req.setQualification("MBBS, MS");
        req.setExperience(8);
        req.setImageUrl("https://hospital.com/images/doctors/john.jpg");

        when(doctorRepository.save(any(Doctor.class))).thenAnswer(invocation -> {
            Doctor doc = invocation.getArgument(0);
            doc.setId(2L);
            return doc;
        });

        DoctorResponse response = doctorService.createDoctor(req);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getName()).isEqualTo("Dr. John Doe");
        assertThat(response.getImageUrl()).isEqualTo("https://hospital.com/images/doctors/john.jpg");
        assertThat(response.getImage_url()).isEqualTo("https://hospital.com/images/doctors/john.jpg");
        verify(doctorRepository).save(any(Doctor.class));
    }

    @Test
    void testCreateDoctor_withSnakeCaseImageUrl_success() {
        CreateDoctorRequest req = new CreateDoctorRequest();
        req.setName("Dr. Alice Smith");
        req.setSpecialization("Pediatrics");
        req.setImage_url("https://hospital.com/images/doctors/alice.jpg");

        when(doctorRepository.save(any(Doctor.class))).thenAnswer(invocation -> {
            Doctor doc = invocation.getArgument(0);
            doc.setId(3L);
            return doc;
        });

        DoctorResponse response = doctorService.createDoctor(req);

        assertThat(response).isNotNull();
        assertThat(response.getImageUrl()).isEqualTo("https://hospital.com/images/doctors/alice.jpg");
        assertThat(response.getImage_url()).isEqualTo("https://hospital.com/images/doctors/alice.jpg");
    }

    @Test
    void testUpdateDoctor_withImageUrl_success() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(sampleDoctor));
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateDoctorRequest req = new UpdateDoctorRequest();
        req.setImageUrl("https://hospital.com/images/doctors/sarah_updated.jpg");

        DoctorResponse response = doctorService.updateDoctor(1L, req);

        assertThat(response).isNotNull();
        assertThat(response.getImageUrl()).isEqualTo("https://hospital.com/images/doctors/sarah_updated.jpg");
        assertThat(sampleDoctor.getImageUrl()).isEqualTo("https://hospital.com/images/doctors/sarah_updated.jpg");
    }

    @Test
    void testUpdateDoctor_withBlankImageUrl_clearsImage() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(sampleDoctor));
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateDoctorRequest req = new UpdateDoctorRequest();
        req.setImageUrl("   ");

        DoctorResponse response = doctorService.updateDoctor(1L, req);

        assertThat(response).isNotNull();
        assertThat(response.getImageUrl()).isNull();
        assertThat(sampleDoctor.getImageUrl()).isNull();
    }

    @Test
    void testUpdateDoctor_withoutImageUrl_preservesExistingImage() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(sampleDoctor));
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateDoctorRequest req = new UpdateDoctorRequest();
        req.setName("Dr. Sarah Jenkins MD");

        DoctorResponse response = doctorService.updateDoctor(1L, req);

        assertThat(response).isNotNull();
        assertThat(response.getImageUrl()).isEqualTo("https://hospital.com/images/doctors/sarah.jpg");
        assertThat(sampleDoctor.getImageUrl()).isEqualTo("https://hospital.com/images/doctors/sarah.jpg");
    }

    @Test
    void testUpdateDoctorImageUrl_validUrl_success() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(sampleDoctor));
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DoctorResponse response = doctorService.updateDoctorImageUrl(1L, "https://hospital.com/new_pic.jpg");

        assertThat(response).isNotNull();
        assertThat(response.getImageUrl()).isEqualTo("https://hospital.com/new_pic.jpg");
        assertThat(sampleDoctor.getImageUrl()).isEqualTo("https://hospital.com/new_pic.jpg");
    }

    @Test
    void testUpdateDoctorImageUrl_nullOrBlank_clearsImage() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(sampleDoctor));
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DoctorResponse response = doctorService.updateDoctorImageUrl(1L, null);

        assertThat(response).isNotNull();
        assertThat(response.getImageUrl()).isNull();
        assertThat(sampleDoctor.getImageUrl()).isNull();
    }

    @Test
    void testUpdateDoctorImageUrl_invalidProtocol_throwsBadRequest() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(sampleDoctor));

        assertThatThrownBy(() -> doctorService.updateDoctorImageUrl(1L, "ftp://hospital.com/pic.jpg"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Image URL must start with http:// or https://");
    }

    @Test
    void testUpdateDoctorImageUrl_tooLong_throwsBadRequest() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(sampleDoctor));
        String overlyLongUrl = "https://hospital.com/" + "a".repeat(500);

        assertThatThrownBy(() -> doctorService.updateDoctorImageUrl(1L, overlyLongUrl))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Image URL cannot exceed 500 characters");
    }

    @Test
    void testGetDoctorById_returnsImageUrl() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(sampleDoctor));

        DoctorResponse response = doctorService.getDoctorById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getImageUrl()).isEqualTo("https://hospital.com/images/doctors/sarah.jpg");
        assertThat(response.getImage_url()).isEqualTo("https://hospital.com/images/doctors/sarah.jpg");
    }

    @Test
    void testGetAllDoctors_returnsImageUrl() {
        when(doctorRepository.findAll()).thenReturn(List.of(sampleDoctor));

        List<DoctorResponse> responses = doctorService.getAllDoctors();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getImageUrl()).isEqualTo("https://hospital.com/images/doctors/sarah.jpg");
    }

    @Test
    void testSearchDoctors_returnsImageUrl() {
        when(doctorRepository.findByNameContainingIgnoreCaseAndSpecializationContainingIgnoreCase("Sarah", "Cardio"))
                .thenReturn(List.of(sampleDoctor));

        List<DoctorResponse> responses = doctorService.searchDoctors("Sarah", "Cardio");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getImageUrl()).isEqualTo("https://hospital.com/images/doctors/sarah.jpg");
    }
}
