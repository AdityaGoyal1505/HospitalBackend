package com.example.Hospital.DTO.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
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

    @Size(max = 500, message = "Image URL cannot exceed 500 characters")
    @Pattern(
            regexp = "^(https?://.*)?$",
            message = "Image URL must start with http:// or https://"
    )
    private String imageUrl;

    private Long departmentId;
    private Long userId;

    public String getImage_url() {
        return imageUrl;
    }

    public void setImage_url(String image_url) {
        this.imageUrl = image_url;
    }

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
