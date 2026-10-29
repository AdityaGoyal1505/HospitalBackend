package com.example.Hospital.Service;

import com.example.Hospital.DTO.request.RequestOtpRequest;
import com.example.Hospital.DTO.request.SignupRequest;
import com.example.Hospital.DTO.request.VerifyOtpRequest;
import com.example.Hospital.DTO.request.VerifySignupOtpRequest;
import com.example.Hospital.DTO.response.AuthResponse;
import com.example.Hospital.model.OtpChallenge;
import com.example.Hospital.model.User;
import com.example.Hospital.model.enums.OtpPurpose;
import com.example.Hospital.model.enums.UserRole;
import com.example.Hospital.Repository.OtpChallengeRepository;
import com.example.Hospital.Repository.UserRepository;
import com.example.Hospital.Service.OtpDeliveryService;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class OtpAuthenticationService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final int OTP_TTL_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 5;
    private static final int RESEND_COOLDOWN_SECONDS = 60;

    private final OtpChallengeRepository otpRepository;
    private final UserRepository userRepository;
    private final OtpDeliveryService otpDeliveryService;
    private final JwtService jwtService;
    private final String hmacSecret;

    public OtpAuthenticationService(
            OtpChallengeRepository otpRepository,
            UserRepository userRepository,
            OtpDeliveryService otpDelivery,
            JwtService jwtService,
            @Value("${hospital.auth.otp-hmac-secret}") String hmacSecret) {

        this.otpRepository = otpRepository;
        this.userRepository = userRepository;
        this.otpDeliveryService = otpDelivery;
        this.jwtService = jwtService;
        this.hmacSecret = hmacSecret;

        if (hmacSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("OTP HMAC secret must be at least 32 bytes");
        }
    }

    @Transactional(readOnly = true)
    public boolean isPhoneRegistered(String phone) {
        if (phone == null || phone.isBlank()) {
            return false;
        }
        return userRepository.existsByPhone(phone.trim());
    }

    @Transactional
    public void requestSignupOtp(SignupRequest request) {
        String phone = request.getPhone();

        if (userRepository.existsByPhone(phone)) {
            return;
        }

        String email = request.getEmail();

        if (email != null && !email.isBlank()) {
            email = email.trim().toLowerCase();

            if (userRepository.existsByEmailIgnoreCase(email)) {
                return;
            }
        } else {
            email = null;
        }

        String name = request.getName().trim();

        // Create OTP and store the pending signup details with it.
        issueOtp(phone, OtpPurpose.SIGNUP, name, email);
    }

    @Transactional
    public void requestLoginOtp(RequestOtpRequest request) {
        Optional<User> optionalUser =
                userRepository.findByPhone(request.getPhone());

        // Account must exist and be active.
        if (optionalUser.isEmpty()) {
            return;
        }

        User user = optionalUser.get();

        if (!user.isActive()) {
            return;
        }

        // Allow ADMIN, RECEPTION, DOCTOR, and PATIENT.
        issueOtp(user.getPhone(), OtpPurpose.LOGIN, null, null);
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public AuthResponse verifySignupOtp(VerifySignupOtpRequest request) {
        String phone = request.getPhone();

        OtpChallenge challenge = verifyCode(
                phone,
                OtpPurpose.SIGNUP,
                request.getOtp()
        );

        if (userRepository.existsByPhone(phone)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Phone number is already registered"
            );
        }

        String email = challenge.getSignupEmail();
        String name = challenge.getSignupName();

        if (email != null && userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email is already registered"
            );
        }

        challenge.setConsumedAt(Instant.now());
        otpRepository.save(challenge);

        User user = new User();
        user.setName(name);
        user.setPhone(phone);
        user.setEmail(email);
        user.setRole(UserRole.PATIENT);
        user.setActive(true);
        user.setProfileCompleted(true);
        user.setPasswordHash(null);

        User savedUser = userRepository.save(user);

        return createAuthResponse(savedUser);
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public AuthResponse verifyLoginOtp(VerifyOtpRequest request) {
        OtpChallenge challenge = verifyCode(
                request.getPhone(), OtpPurpose.LOGIN, request.getOtp()
        );

        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid or expired OTP"
                ));

        if (!user.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid or expired OTP"
                );
        }

        challenge.setConsumedAt(Instant.now());
        otpRepository.save(challenge);

        return createAuthResponse(user);
    }

    private void issueOtp(
            String phone,
            OtpPurpose purpose,
            String signupName,
            String signupEmail
    ) {
        Instant now = Instant.now();

        Optional<OtpChallenge> previous =
                otpRepository
                        .findTopByPhoneAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(
                                phone, purpose
                        );

        if (previous.isPresent()) {
            OtpChallenge old = previous.get();

            Duration sinceLastSend =
                    Duration.between(old.getLastSentAt(), now);

            if (sinceLastSend.getSeconds() < RESEND_COOLDOWN_SECONDS) {
                // Silent cooldown keeps the public response generic.
                return;
            }

            old.setConsumedAt(now);
            otpRepository.save(old);
        }

        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));

        OtpChallenge challenge = new OtpChallenge();
        challenge.setPhone(phone);
        challenge.setPurpose(purpose);
        challenge.setOtpHash(hashOtp(phone, purpose, otp));
        challenge.setExpiresAt(now.plusSeconds(OTP_TTL_MINUTES * 60L));
        challenge.setAttempts(0);
        challenge.setLastSentAt(now);
        challenge.setSignupName(signupName);
        challenge.setSignupEmail(signupEmail);

        otpRepository.save(challenge);

        // If sending fails, the transaction rolls back.
        otpDeliveryService.sendOtp(phone, otp);
    }

    private OtpChallenge verifyCode(
            String phone, OtpPurpose purpose, String submittedOtp) {

        OtpChallenge challenge = otpRepository
                .findTopByPhoneAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(
                        phone, purpose
                )
                .orElseThrow(() -> invalidOtp());

        Instant now = Instant.now();

        if (challenge.getAttempts() >= MAX_ATTEMPTS) {
            challenge.setConsumedAt(now);
            otpRepository.save(challenge);
            throw invalidOtp();
        }

        if (!challenge.getExpiresAt().isAfter(now)) {
            challenge.setConsumedAt(now);
            otpRepository.save(challenge);
            throw invalidOtp();
        }

        String submittedHash = hashOtp(phone, purpose, submittedOtp);

        boolean matches = MessageDigest.isEqual(
                challenge.getOtpHash().getBytes(StandardCharsets.US_ASCII),
                submittedHash.getBytes(StandardCharsets.US_ASCII)
        );

        if (!matches) {
            challenge.setAttempts(challenge.getAttempts() + 1);

            if (challenge.getAttempts() >= MAX_ATTEMPTS) {
                challenge.setConsumedAt(now);
            }

            otpRepository.save(challenge);
            throw invalidOtp();
        }

        return challenge;
    }

    private String hashOtp(
            String phone, OtpPurpose purpose, String otp) {

        try {
            Mac mac = Mac.getInstance("HmacSHA256");

            mac.init(new SecretKeySpec(
                    hmacSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            ));

            String value = phone + "|" + purpose.name() + "|" + otp;

            return HexFormat.of().formatHex(
                    mac.doFinal(value.getBytes(StandardCharsets.UTF_8))
            );
        } catch (Exception ex) {
            throw new IllegalStateException("OTP processing failed");
        }
    }

    private AuthResponse createAuthResponse(User user) {
        AuthResponse response = new AuthResponse();

        response.setAccessToken(jwtService.generateToken(user));
        response.setTokenType("Bearer");
        response.setExpiresInSeconds(jwtService.getExpirationSeconds());
        response.setUserId(user.getId());
        response.setName(user.getName());
        response.setRole(user.getRole());

        return response;
    }

    private ResponseStatusException invalidOtp() {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Invalid or expired OTP"
        );
    }
}
