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
    private String imageUrl;
    private String image_url;

    private Long department_id;
    private String department_name;
    private Long user_id;
    private String user_name;
    private String user_phone;
    private String user_email;

    public String getImageUrl() {
        return imageUrl != null ? imageUrl : image_url;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        this.image_url = imageUrl;
    }

    public String getImage_url() {
        return image_url != null ? image_url : imageUrl;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
        this.imageUrl = image_url;
    }

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
