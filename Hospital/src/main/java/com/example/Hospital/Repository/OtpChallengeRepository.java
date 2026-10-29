package com.example.Hospital.Repository;

import com.example.Hospital.model.OtpChallenge;
import com.example.Hospital.model.enums.OtpPurpose;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface OtpChallengeRepository extends JpaRepository<OtpChallenge, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<OtpChallenge>
    findTopByPhoneAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(String phone, OtpPurpose purpose);
}
