
package com.example.Hospital.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestClient;

@Configuration
@Profile("!local")
public class WhatsAppConfig {

    @Bean
    public RestClient whatsAppRestClient(
            @Value("${whatsapp.api.base-url}") String baseUrl,
            @Value("${whatsapp.api.access-token}") String accessToken) {

        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + accessToken)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
