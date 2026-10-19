package za.ac.cput.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.cput.domain.Notification;
import za.ac.cput.domain.User;
import za.ac.cput.repository.NotificationRepository;
import za.ac.cput.repository.UserRepository;

import java.util.List;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository) {

        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create and save a notification for a user.
     */
    public Notification createNotification(
            Long userId,
            String message,
            String type) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with ID: " + userId
                        )
                );

        Notification notification = Notification.builder()
                .user(user)
                .message(message)
                .type(type)
                .read(false)
                .build();

        return notificationRepository.save(notification);
    }

    /**
     * Get all notifications for a user.
     */
    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(Long userId) {

        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Get only unread notifications for a user.
     */
    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(Long userId) {

        return notificationRepository
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
    }

    /**
     * Get the number of unread notifications.
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {

        return notificationRepository
                .countByUserIdAndReadFalse(userId);
    }

    /**
     * Mark one notification as read.
     */
    public Notification markAsRead(Long notificationId) {

        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Notification not found with ID: "
                                        + notificationId
                        )
                );

        notification.setRead(true);

        return notificationRepository.save(notification);
    }

    /**
     * Mark all notifications for a user as read.
     */
    public void markAllAsRead(Long userId) {

        List<Notification> notifications =
                notificationRepository
                        .findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);

        notifications.forEach(notification ->
                notification.setRead(true)
        );

        notificationRepository.saveAll(notifications);
    }

    /**
     * Delete one notification.
     */
    public void deleteNotification(Long notificationId) {

        if (!notificationRepository.existsById(notificationId)) {
            throw new RuntimeException(
                    "Notification not found with ID: "
                            + notificationId
            );
        }

        notificationRepository.deleteById(notificationId);
    }
}