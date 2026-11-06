package com.example.Hospital.service.impl;

import com.example.Hospital.model.enums.PushResult;
import com.example.Hospital.service.PushSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "app.push", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoOpPushSender implements PushSender {

    @Override
    public PushResult send(String deviceToken, String title, String body) {

        log.info("Push skipped (push is disabled)");
        return PushResult.SKIPPED;
    }
}
