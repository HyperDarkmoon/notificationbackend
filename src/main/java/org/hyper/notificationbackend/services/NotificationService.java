package org.hyper.notificationbackend.services;

import org.hyper.notificationbackend.models.Notification;
import org.hyper.notificationbackend.models.TV;
import org.hyper.notificationbackend.models.User;
import org.hyper.notificationbackend.repositories.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public Notification createNotification(String title, String message, User user, TV tv) {
        Notification notification = new Notification(title, message, user);
        if (tv != null) {
            notification.setTv(tv);
        }
        return notificationRepository.save(notification);
    }

    public List<Notification> getNotificationsByUser(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<Notification> getUnreadNotificationsByUser(User user) {
        return notificationRepository.findUnreadNotificationsByUser(user);
    }

    public List<Notification> getNotificationsByUserAndReadStatus(User user, Boolean isRead) {
        return notificationRepository.findByUserAndIsReadOrderByCreatedAtDesc(user, isRead);
    }

    public Long countUnreadNotifications(User user) {
        return notificationRepository.countByUserAndIsRead(user, false);
    }

    public Optional<Notification> findById(Long id) {
        return notificationRepository.findById(id);
    }

    public Notification markAsRead(Long notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.setIsRead(true);
            return notificationRepository.save(notification);
        }
        throw new RuntimeException("Notification not found with id: " + notificationId);
    }

    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }
}
