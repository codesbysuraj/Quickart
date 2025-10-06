package com.quickkart.controller;

import com.quickkart.entity.Notification;
import com.quickkart.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    // Get notifications for a vendor
    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<Notification>> getVendorNotifications(@PathVariable Long vendorId) {
        return ResponseEntity.ok(notificationRepository.findByVendorIdOrderByCreatedAtDesc(vendorId));
    }

    // Mark notification as read
    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markRead(@PathVariable Long id) {
        return notificationRepository.findById(id).map(n -> {
            n.setReadFlag(true);
            return ResponseEntity.ok(notificationRepository.save(n));
        }).orElse(ResponseEntity.notFound().build());
    }
}
