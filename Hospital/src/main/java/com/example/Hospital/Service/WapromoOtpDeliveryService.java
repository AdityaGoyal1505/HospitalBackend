package com.example.Hospital.Service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("wapromo")
public class WapromoOtpDeliveryService implements OtpDeliveryService {

    @Override
    public void sendOtp(String phone, String otp) {
        // Call Wapromo SendCampaigns API
    }
}