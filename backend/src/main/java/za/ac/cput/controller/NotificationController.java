package za.ac.cput.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import za.ac.cput.domain.Notification;
import za.ac.cput.service.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(
            NotificationService notificationService) {

        this.notificationService = notificationService;
    }

    /* =========================================================
       GET ALL NOTIFICATIONS
    ========================================================= */

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getUserNotifications(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                notificationService.getUserNotifications(userId)
        );
    }

    /* =========================================================
       GET UNREAD NOTIFICATIONS
    ========================================================= */

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                notificationService.getUnreadNotifications(userId)
        );
    }

    /* =========================================================
       GET UNREAD COUNT
    ========================================================= */

    @GetMapping("/user/{userId}/unread/count")
    public ResponseEntity<Long> getUnreadCount(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                notificationService.getUnreadCount(userId)
        );
    }

    /* =========================================================
       MARK ONE NOTIFICATION AS READ
    ========================================================= */

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Notification> markAsRead(
            @PathVariable Long notificationId) {

        return ResponseEntity.ok(
                notificationService.markAsRead(
                        notificationId
                )
        );
    }

    /* =========================================================
       MARK ALL AS READ
    ========================================================= */

    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @PathVariable Long userId) {

        notificationService.markAllAsRead(userId);

        return ResponseEntity.noContent().build();
    }

    /* =========================================================
       DELETE NOTIFICATION
    ========================================================= */

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long notificationId) {

        notificationService.deleteNotification(
                notificationId
        );

        return ResponseEntity.noContent().build();
    }
}