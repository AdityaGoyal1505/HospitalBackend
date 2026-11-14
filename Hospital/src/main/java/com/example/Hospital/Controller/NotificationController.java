package com.example.Hospital.Controller;

import com.example.Hospital.Config.CurrentUser;
import com.example.Hospital.DTO.NotificationResponseDTO;
import com.example.Hospital.Service.NotificationService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;



    @GetMapping
    public ResponseEntity<Page<NotificationResponseDTO>> list(
            @RequestParam(name = "unreadOnly", defaultValue = "false") boolean unreadOnly,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(notificationService.list(CurrentUser.id(), unreadOnly, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getById(CurrentUser.id(), id));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount() {
        return ResponseEntity.ok(Map.of("unreadCount", notificationService.countUnread(CurrentUser.id())));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponseDTO> markRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markRead(CurrentUser.id(), id));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllRead() {
        notificationService.markAllRead(CurrentUser.id());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        notificationService.delete(CurrentUser.id(), id);
        return ResponseEntity.noContent().build();
    }
}