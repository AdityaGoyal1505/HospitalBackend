package com.example.Hospital.DTO.request;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateDoctorRequest {

    private String name;

    private String specialization;

    private String qualification;

    @PositiveOrZero(message = "Experience must be zero or a positive number")
    private Integer experience;

    private Long departmentId;
    private Long userId;

    public Long getUser_id() {
        return userId;
    }

    public void setUser_id(Long user_id) {
        this.userId = user_id;
    }

    public Long getDepartment_id() {
        return departmentId;
    }

    public void setDepartment_id(Long department_id) {
        this.departmentId = department_id;
    }
}
