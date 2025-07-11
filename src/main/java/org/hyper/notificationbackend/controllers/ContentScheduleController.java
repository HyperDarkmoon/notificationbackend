package org.hyper.notificationbackend.controllers;

import org.hyper.notificationbackend.models.ContentSchedule;
import org.hyper.notificationbackend.models.TVEnum;
import org.hyper.notificationbackend.services.ContentScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
}
