package org.hyper.notificationbackend.controllers;

import org.hyper.notificationbackend.models.ContentSchedule;
import org.hyper.notificationbackend.models.TVEnum;
import org.hyper.notificationbackend.services.ContentScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/content")
public class ContentScheduleController {

    @Autowired
    private ContentScheduleService contentScheduleService;
    
    // Create a new content schedule
    @PostMapping
    public ResponseEntity<?> createContentSchedule(@RequestBody ContentSchedule contentSchedule) {
        try {
            ContentSchedule newSchedule = contentScheduleService.createSchedule(contentSchedule);
            return ResponseEntity.ok(newSchedule);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // Get all content schedules
    @GetMapping("/all")
    public ResponseEntity<List<ContentSchedule>> getAllContentSchedules() {
        return ResponseEntity.ok(contentScheduleService.getAllSchedules());
    }
    
    // Get content schedule by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getContentScheduleById(@PathVariable("id") Long id) {
        Optional<ContentSchedule> schedule = contentScheduleService.getScheduleById(id);
        return schedule.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Get currently active schedules
    @GetMapping("/active")
    public ResponseEntity<List<ContentSchedule>> getCurrentlyActiveSchedules() {
        return ResponseEntity.ok(contentScheduleService.getCurrentlyActiveSchedules());
    }
    
    // Get upcoming schedules
    @GetMapping("/upcoming")
    public ResponseEntity<List<ContentSchedule>> getUpcomingSchedules() {
        return ResponseEntity.ok(contentScheduleService.getUpcomingSchedules());
    }
    
    // Get immediate/indefinite schedules
    @GetMapping("/immediate")
    public ResponseEntity<List<ContentSchedule>> getImmediateSchedules() {
        return ResponseEntity.ok(contentScheduleService.getImmediateSchedules());
    }
    
    // Get schedules for a specific TV
    @GetMapping("/tv/{tvName}")
    public ResponseEntity<?> getSchedulesForTV(@PathVariable("tvName") String tvName) {
        try {
            TVEnum tv = TVEnum.valueOf(tvName);
            return ResponseEntity.ok(contentScheduleService.getSchedulesForTV(tv));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid TV name: " + tvName);
        }
    }
    
    // Get upcoming schedules for a specific TV
    @GetMapping("/tv/{tvName}/upcoming")
    public ResponseEntity<?> getUpcomingSchedulesForTV(@PathVariable("tvName") String tvName) {
        try {
            TVEnum tv = TVEnum.valueOf(tvName);
            return ResponseEntity.ok(contentScheduleService.getUpcomingSchedulesForTV(tv));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid TV name: " + tvName);
        }
    }
    
    // Update a content schedule
    @PutMapping("/{id}")
    public ResponseEntity<?> updateContentSchedule(@PathVariable("id") Long id, @RequestBody ContentSchedule contentSchedule) {
        try {
            ContentSchedule updatedSchedule = contentScheduleService.updateSchedule(id, contentSchedule);
            return ResponseEntity.ok(updatedSchedule);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // Delete a content schedule
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteContentSchedule(@PathVariable("id") Long id) {
        try {
            contentScheduleService.deleteSchedule(id);
            return ResponseEntity.ok("Content schedule deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // Manual trigger to clean up expired content
    @PostMapping("/cleanup-expired")
    public ResponseEntity<?> cleanupExpiredContent() {
        try {
            contentScheduleService.restoreTemporarilyDisabledContent();
            return ResponseEntity.ok("Expired content cleaned up successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // Debug endpoint to check TV content status
    @GetMapping("/debug/tv/{tvName}")
    public ResponseEntity<?> debugTVContent(@PathVariable("tvName") String tvName) {
        try {
            TVEnum tv = TVEnum.valueOf(tvName);
            LocalDateTime now = LocalDateTime.now();
            
            // Get all schedules for this TV
            List<ContentSchedule> allSchedules = contentScheduleService.getAllSchedules().stream()
                .filter(s -> s.getTargetTVs().contains(tv))
                .toList();
            
            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("currentTime", now);
            debugInfo.put("totalSchedulesForTV", allSchedules.size());
            debugInfo.put("activeSchedules", allSchedules.stream().filter(s -> s.isActive()).count());
            debugInfo.put("timedSchedules", allSchedules.stream()
                .filter(s -> s.isActive() && s.getStartTime() != null && s.getEndTime() != null)
                .count());
            debugInfo.put("immediateSchedules", allSchedules.stream()
                .filter(s -> s.isActive() && s.getStartTime() == null && s.getEndTime() == null)
                .count());
            
            // Check current content
            List<ContentSchedule> currentContent = contentScheduleService.getSchedulesForTV(tv);
            debugInfo.put("currentContentCount", currentContent.size());
            if (!currentContent.isEmpty()) {
                debugInfo.put("currentContentTitle", currentContent.get(0).getTitle());
                debugInfo.put("currentContentType", currentContent.get(0).getContentType());
            }
            
            // List all schedules with details
            List<Map<String, Object>> scheduleDetails = allSchedules.stream()
                .map(s -> {
                    Map<String, Object> details = new HashMap<>();
                    details.put("id", s.getId());
                    details.put("title", s.getTitle());
                    details.put("active", s.isActive());
                    details.put("startTime", s.getStartTime());
                    details.put("endTime", s.getEndTime());
                    details.put("contentType", s.getContentType());
                    return details;
                })
                .toList();
            debugInfo.put("allSchedules", scheduleDetails);
            
            return ResponseEntity.ok(debugInfo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid TV name: " + tvName);
        }
    }
}
