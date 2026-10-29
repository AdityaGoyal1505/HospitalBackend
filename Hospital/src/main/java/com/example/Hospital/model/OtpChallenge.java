package com.example.Hospital.model;

import com.example.Hospital.model.enums.OtpPurpose;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "otp_challenges",
        indexes = {
                @Index(
                        name = "idx_otp_phone_purpose_created",
                        columnList = "phone, purpose, created_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class OtpChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 16)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OtpPurpose purpose;

    // HMAC hash of the OTP; never store the plain code.
    @Column(nullable = false, length = 64)
    private String otpHash;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private int attempts = 0;

    // Non-null means this challenge cannot be used again.
    private Instant consumedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant lastSentAt;

    @Column(name = "signup_name")
    private String signupName;

    @Column(name = "signup_email")
    private String signupEmail;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
