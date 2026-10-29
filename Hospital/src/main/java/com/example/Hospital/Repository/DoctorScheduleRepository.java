
package com.example.Hospital.Repository;

import com.example.Hospital.model.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {

    List<DoctorSchedule> findByDoctor_IdAndActiveTrue(Long doctorId);

    List<DoctorSchedule> findByDoctor_IdAndDayOfWeekAndActiveTrue(Long doctorId, DayOfWeek dayOfWeek);
    List<DoctorSchedule> findByDayOfWeekAndActiveTrue(DayOfWeek dayOfWeek);

    List<DoctorSchedule> findByDoctor_IdAndDayOfWeekAndActiveTrueAndIdNot(Long doctorId, DayOfWeek dayOfWeek, Long id);

    List<DoctorSchedule> findByDoctor_IdAndDayOfWeekAndActiveTrueAndValidFromLessThanEqualAndValidUntilGreaterThanEqual(Long doctorId, DayOfWeek dayOfWeek, LocalDate date1, LocalDate date2);
}
