package com.example.Hospital.event;

public record NotificationCreatedEvent(Long userId, String title, String message) {
}