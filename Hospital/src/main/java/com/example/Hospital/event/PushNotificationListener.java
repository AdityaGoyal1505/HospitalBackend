package com.example.Hospital.event;

import com.example.Hospital.model.DeviceToken;
import com.example.Hospital.model.enums.PushResult;
import com.example.Hospital.repository.DeviceTokenRepository;
import com.example.Hospital.service.PushSender;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushNotificationListener {

    private final DeviceTokenRepository deviceTokenRepository;
    private final PushSender pushSender;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onNotificationCreated(NotificationCreatedEvent event) {
        try {
            List<DeviceToken> devices = deviceTokenRepository.findByUserId(event.userId());
            for (DeviceToken device : devices) {
                PushResult result = pushSender.send(device.getToken(), event.title(), event.message());
                if (result == PushResult.INVALID_TOKEN) {
                    deviceTokenRepository.delete(device);
                }
            }
        } catch (RuntimeException e) {
            log.warn("Push delivery failed for user {} ({})", event.userId(), e.getClass().getSimpleName());
        }
    }
}

