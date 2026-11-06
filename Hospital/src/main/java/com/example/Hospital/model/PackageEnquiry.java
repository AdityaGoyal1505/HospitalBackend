package com.example.Hospital.model;

import com.example.Hospital.model.enums.PackageEnquiryStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "package_enquiries",
        indexes = {
                @Index(name = "idx_pkg_enquiry_user_id", columnList = "user_id"),
                @Index(name = "idx_pkg_enquiry_phone", columnList = "phone"),
                @Index(name = "idx_pkg_enquiry_status", columnList = "status"),
                @Index(name = "idx_pkg_enquiry_created_at", columnList = "createdAt")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class PackageEnquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The authenticated user who submitted the enquiry
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 180)
    private String email;

    @Column(name = "package_name", nullable = false, length = 150)
    private String packageName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id")
    private MedicalPackage medicalPackage;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PackageEnquiryStatus status = PackageEnquiryStatus.PENDING;

    @Column(name = "contact_notes", columnDefinition = "TEXT")
    private String contactNotes;

    // Staff member (Reception / Admin) who contacted the applicant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contacted_by_user_id")
    private User contactedBy;

    @Column(name = "contacted_at")
    private Instant contactedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = PackageEnquiryStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
