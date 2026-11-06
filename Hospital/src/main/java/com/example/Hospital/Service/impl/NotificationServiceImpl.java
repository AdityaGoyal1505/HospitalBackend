package com.example.Hospital.service.impl;

import com.example.Hospital.dto.NotificationResponseDTO;
import com.example.Hospital.model.Notification;
import com.example.Hospital.model.enums.NotificationType;
import com.example.Hospital.event.NotificationCreatedEvent;
import com.example.Hospital.exception.ResourceNotFoundException;
import com.example.Hospital.repository.NotificationRepository;
import com.example.Hospital.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final int MAX_TITLE = 150;
    private static final int MAX_MESSAGE = 500;

    private final NotificationRepository notificationRepository;
    private final ApplicationEventPublisher eventPublisher;

    private NotificationResponseDTO toResponse(Notification notification) {
        NotificationResponseDTO dto = new NotificationResponseDTO();
        dto.setId(notification.getId());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }

    private static void requirePositive(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User id must be positive");
        }
    }

    @Override
    @Transactional
    public NotificationResponseDTO create(
            Long userId, String title, String message, NotificationType type) {

        requirePositive(userId);
        if (type == null) {
            throw new IllegalArgumentException("Notification type is required");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message is required");
        }

        String cleanTitle = title.trim();
        String cleanMessage = message.trim();
        if (cleanTitle.length() > MAX_TITLE) {
            throw new IllegalArgumentException("Title must be at most " + MAX_TITLE + " characters");
        }
        if (cleanMessage.length() > MAX_MESSAGE) {
            throw new IllegalArgumentException("Message must be at most " + MAX_MESSAGE + " characters");
        }

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(cleanTitle);
        notification.setMessage(cleanMessage);
        notification.setType(type);
        notification.setRead(false);

        Notification saved = notificationRepository.save(notification);
        eventPublisher.publishEvent(
                new NotificationCreatedEvent(saved.getUserId(), saved.getTitle(), saved.getMessage()));
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponseDTO getById(Long userId, Long notificationId) {
        requirePositive(userId);
        Notification notification = notificationRepository
                .findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found with id " + notificationId));
        return toResponse(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponseDTO> list(Long userId, boolean unreadOnly, Pageable pageable) {
        requirePositive(userId);

        Page<Notification> result = unreadOnly
                ? notificationRepository.findByUserIdAndReadFalse(userId, pageable)
                : notificationRepository.findByUserId(userId, pageable);

        return result.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        requirePositive(userId);
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Override
    @Transactional
    public NotificationResponseDTO markRead(Long userId, Long notificationId) {
        requirePositive(userId);

        Notification notification = notificationRepository
                .findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found with id " + notificationId));

        notification.setRead(true);
        return toResponse(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    public void markAllRead(Long userId) {
        requirePositive(userId);
        notificationRepository.markAllReadByUserId(userId);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long notificationId) {
        requirePositive(userId);
        Notification notification = notificationRepository
                .findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found with id " + notificationId));
        notificationRepository.delete(notification);
    }
}

