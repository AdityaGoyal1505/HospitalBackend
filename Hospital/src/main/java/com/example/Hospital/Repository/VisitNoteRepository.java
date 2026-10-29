
package com.example.Hospital.Repository;

import com.example.Hospital.model.VisitNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VisitNoteRepository extends JpaRepository<VisitNote, Long> {

    List<VisitNote> findByPatient_IdOrderByCreatedAtDesc(Long patientId);

    List<VisitNote> findByDoctor_IdOrderByCreatedAtDesc(Long doctorId);

    List<VisitNote> findByAppointment_Id(Long appointmentId);

    boolean existsByAppointment_Id(Long appointmentId);

    Optional<VisitNote> findByIdAndDoctor_Id(Long id, Long doctorId);
}
