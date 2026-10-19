package za.ac.cput.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.cput.domain.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Get all notifications for a specific user,
     * newest notifications first.
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Get only unread notifications for a user.
     */
    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(Long userId);

    /**
     * Count unread notifications for a user.
     */
    long countByUserIdAndReadFalse(Long userId);
}