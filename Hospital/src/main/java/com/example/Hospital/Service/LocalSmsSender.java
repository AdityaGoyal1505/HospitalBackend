
package com.example.Hospital.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("local")
public class LocalSmsSender implements OtpDeliveryService {

    private static final Logger log =
            LoggerFactory.getLogger(LocalSmsSender.class);

    @Override
    public void sendOtp(String phone, String message) {
        // LOCAL TESTING ONLY: never enable in production.
        log.info("[LOCAL OTP TEST] Phone: {}, Message: {}", phone, message);
    }
}
