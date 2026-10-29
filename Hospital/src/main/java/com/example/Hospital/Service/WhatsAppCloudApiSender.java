
package com.example.Hospital.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@Profile("!local")
public class WhatsAppCloudApiSender implements OtpDeliveryService {

    private final RestClient whatsAppRestClient;
    private final String apiVersion;
    private final String phoneNumberId;
    private final String otpTemplateName;
    private final String templateLanguage;

    public WhatsAppCloudApiSender(
            RestClient whatsAppRestClient,
            @Value("${whatsapp.api.version}") String apiVersion,
            @Value("${whatsapp.api.phone-number-id}") String phoneNumberId,
            @Value("${whatsapp.api.otp-template-name}") String otpTemplateName,
            @Value("${whatsapp.api.template-language:en_US}") String templateLanguage) {

        this.whatsAppRestClient = whatsAppRestClient;
        this.apiVersion = apiVersion;
        this.phoneNumberId = phoneNumberId;
        this.otpTemplateName = otpTemplateName;
        this.templateLanguage = templateLanguage;
    }

    @Override
    public void sendOtp(String phone, String otp) {

        Map<String, Object> payload = Map.of(
                "messaging_product", "whatsapp",
                "to", phone.startsWith("+") ? phone.substring(1) : phone,
                "type", "template",
                "template", Map.of(
                        "name", otpTemplateName,
                        "language", Map.of(
                                "code", templateLanguage
                        ),
                        "components", new Object[]{
                                Map.of(
                                        "type", "body",
                                        "parameters", new Object[]{
                                                Map.of(
                                                        "type", "text",
                                                        "text", otp
                                                )
                                        }
                                )
                        }
                )
        );

        try {
            whatsAppRestClient.post()
                    .uri("/{version}/{phoneNumberId}/messages",
                            apiVersion, phoneNumberId)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

        } catch (RuntimeException ex) {
            // Do not log OTPs, access tokens, or message payloads.
            throw new IllegalStateException(
                    "Unable to send WhatsApp verification message"
            );
        }
    }
}
