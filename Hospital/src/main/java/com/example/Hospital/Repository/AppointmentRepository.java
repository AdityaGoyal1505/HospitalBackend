
package com.example.Hospital.Repository;

import com.example.Hospital.model.Appointment;
import com.example.Hospital.model.DoctorSchedule;
import com.example.Hospital.model.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface AppointmentRepository
        extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatient_IdOrderByCreatedAtDesc(Long patientId);

    List<Appointment> findByDoctor_IdOrderByStartAtAsc(Long doctorId);

    List<Appointment> findByStatusOrderByCreatedAtDesc(AppointmentStatus status);

    boolean existsByDoctor_IdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
            Long doctorId,
            Collection<AppointmentStatus> statuses,
            Instant requestedEnd,
            Instant requestedStart
    );

    List<Appointment> findByDoctor_IdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
            Long doctorId,
            Collection<AppointmentStatus> statuses,
            Instant requestedEnd,
            Instant requestedStart
    );

    List<Appointment> findByPatient_IdAndStatusIn(Long patientId, Collection<AppointmentStatus> statuses);
}
