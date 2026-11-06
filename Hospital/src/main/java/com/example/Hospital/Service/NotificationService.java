package com.example.Hospital.service;

import com.example.Hospital.dto.NotificationResponseDTO;
import com.example.Hospital.model.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    NotificationResponseDTO create(Long userId, String title, String message, NotificationType type);

    NotificationResponseDTO getById(Long userId, Long notificationId);

    Page<NotificationResponseDTO> list(Long userId, boolean unreadOnly, Pageable pageable);

    long countUnread(Long userId);

    NotificationResponseDTO markRead(Long userId, Long notificationId);

    void markAllRead(Long userId);

    void delete(Long userId, Long notificationId);
}

