package com.example.Hospital.DTO.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DoctorResponse {

    private Long id;
    private String name;
    private String specialization;
    private String qualification;
    private Integer experience;

    private Long department_id;
    private String department_name;
    private Long user_id;
    private String user_name;
    private String user_phone;
    private String user_email;

    public int getDoctor_id() {
        return id != null ? id.intValue() : 0;
    }

    public void setDoctor_id(int doctor_id) {
        this.id = (long) doctor_id;
    }

    public String getSpeciality() {
        return specialization;
    }

    public void setSpeciality(String speciality) {
        this.specialization = speciality;
    }
}
