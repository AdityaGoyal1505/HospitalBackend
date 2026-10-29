package com.example.Hospital.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "doctors",
        indexes = {
                @Index(name = "idx_doctor_name", columnList = "name"),
                @Index(name = "idx_doctor_specialization", columnList = "specialization")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 120)
    private String specialization;

    @Column(length = 150)
    private String qualification;

    @Column
    private Integer experience;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Doctor(String name, String specialization, String qualification, Integer experience) {
        this.name = name;
        this.specialization = specialization;
        this.qualification = qualification;
        this.experience = experience;
    }
}
