package org.hyper.notificationbackend.controllers;

import org.hyper.notificationbackend.models.Notification;
import org.hyper.notificationbackend.models.TV;
import org.hyper.notificationbackend.models.User;
import org.hyper.notificationbackend.services.NotificationService;
import org.hyper.notificationbackend.services.TVService;
import org.hyper.notificationbackend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*", maxAge = 3600)
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @Autowired
    private TVService tvService;

    @GetMapping
    public ResponseEntity<?> getAllNotificationsForCurrentUser() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body("Error: User not authenticated!");
            }

            List<Notification> notifications = notificationService.getNotificationsByUser(currentUser);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadNotifications() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body("Error: User not authenticated!");
            }

            List<Notification> notifications = notificationService.getUnreadNotificationsByUser(currentUser);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/count/unread")
    public ResponseEntity<?> getUnreadNotificationCount() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body("Error: User not authenticated!");
            }

            Long count = notificationService.countUnreadNotifications(currentUser);
            Map<String, Object> response = new HashMap<>();
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createNotification(@RequestBody NotificationRequest request) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body("Error: User not authenticated!");
            }

            TV tv = null;
            if (request.getTvId() != null) {
                Optional<TV> tvOpt = tvService.findById(request.getTvId());
                if (tvOpt.isPresent()) {
                    tv = tvOpt.get();
                }
            }

            Notification notification = notificationService.createNotification(
                    request.getTitle(),
                    request.getMessage(),
                    currentUser,
                    tv
            );

            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        try {
            Notification notification = notificationService.markAsRead(id);
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        try {
            notificationService.deleteNotification(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Notification deleted successfully!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Optional<User> userOpt = userService.findByUsername(username);
            return userOpt.orElse(null);
        }
        return null;
    }

    // Inner class for request body
    public static class NotificationRequest {
        private String title;
        private String message;
        private Long tvId;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Long getTvId() {
            return tvId;
        }

        public void setTvId(Long tvId) {
            this.tvId = tvId;
        }
    }
}
