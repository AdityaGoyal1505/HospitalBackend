package com.example.Hospital.controller;

import com.example.Hospital.dto.NotificationResponseDTO;
import com.example.Hospital.service.NotificationService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Page<NotificationResponseDTO>> list(
            @RequestParam("userId") Long userId,
            @RequestParam(name = "unreadOnly", defaultValue = "false") boolean unreadOnly,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(notificationService.list(userId, unreadOnly, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponseDTO> getById(
            @PathVariable Long id,
            @RequestParam("userId") Long userId) {
        return ResponseEntity.ok(notificationService.getById(userId, id));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount(@RequestParam("userId") Long userId) {
        return ResponseEntity.ok(Map.of("unreadCount", notificationService.countUnread(userId)));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponseDTO> markRead(
            @PathVariable Long id,
            @RequestParam("userId") Long userId) {
        return ResponseEntity.ok(notificationService.markRead(userId, id));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllRead(@RequestParam("userId") Long userId) {
        notificationService.markAllRead(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam("userId") Long userId) {
        notificationService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
