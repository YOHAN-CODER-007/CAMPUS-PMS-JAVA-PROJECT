package com.placement.service;

import com.placement.model.Notification;
import com.placement.model.Role;
import com.placement.model.User;
import com.placement.repository.NotificationRepository;
import com.placement.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service handling system in-app notifications.
 */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates and saves a new notification for a specific user.
     */
    @Transactional
    public void createNotification(User user, String message) {
        if (user == null || message == null || message.trim().isEmpty()) {
            return;
        }
        Notification notification = new Notification(user, message.trim());
        notificationRepository.save(notification);
    }

    /**
     * Broadcasts an in-app tracking notification to all Placement Officers (Admins).
     */
    @Transactional
    public void notifyOfficers(String message) {
        if (message == null || message.trim().isEmpty()) {
            return;
        }
        List<User> officers = userRepository.findByRole(Role.ROLE_OFFICER);
        for (User officer : officers) {
            Notification notification = new Notification(officer, message.trim());
            notificationRepository.save(notification);
        }
    }

    /**
     * Fetches all notifications for a user, newest first.
     */
    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * Marks all notifications as read for a user.
     */
    @Transactional
    public void markAllAsRead(User user) {
        List<Notification> unreadList = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        for (Notification n : unreadList) {
            if (!n.isRead()) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        }
    }
}
