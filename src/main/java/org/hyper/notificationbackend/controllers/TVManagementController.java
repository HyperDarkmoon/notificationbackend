package org.hyper.notificationbackend.controllers;

import org.hyper.notificationbackend.models.TV;
import org.hyper.notificationbackend.services.TVService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tvs")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TVManagementController {

    @Autowired
    private TVService tvService;
    
    // Get all TVs (admin only)
    @GetMapping
    public ResponseEntity<List<TV>> getAllTVs() {
        List<TV> tvs = tvService.getAllTVs();
        return ResponseEntity.ok(tvs);
    }
    
    // Get all active TVs (public access for TV display selection)
    @GetMapping("/active")
    public ResponseEntity<List<TV>> getAllActiveTVs() {
        List<TV> tvs = tvService.getAllActiveTVs();
        return ResponseEntity.ok(tvs);
    }
    
    // Get TV by ID (admin only)
    @GetMapping("/{id}")
    public ResponseEntity<?> getTVById(@PathVariable("id") Long id) {
        try {
            Optional<TV> tv = tvService.getTVById(id);
            return tv.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // Get TV by name (public access)
    @GetMapping("/name/{name}")
    public ResponseEntity<?> getTVByName(@PathVariable("name") String name) {
        try {
            Optional<TV> tv = tvService.getActiveTVByName(name);
            return tv.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // Create new TV (admin only)
    @PostMapping
    public ResponseEntity<?> createTV(@RequestBody TV tv) {
        try {
            TV newTV = tvService.createTV(tv);
            return ResponseEntity.ok(newTV);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: Failed to create TV");
        }
    }
    
    // Update TV (admin only)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTV(@PathVariable("id") Long id, @RequestBody TV tv) {
        try {
            TV updatedTV = tvService.updateTV(id, tv);
            return ResponseEntity.ok(updatedTV);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: Failed to update TV");
        }
    }
    
    // Delete TV (soft delete - admin only)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTV(@PathVariable("id") Long id) {
        try {
            tvService.deleteTV(id);
            return ResponseEntity.ok().body("TV deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: Failed to delete TV");
        }
    }
    
    // Permanently delete TV (admin only - use with extreme caution)
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<?> permanentlyDeleteTV(@PathVariable("id") Long id) {
        try {
            tvService.permanentlyDeleteTV(id);
            return ResponseEntity.ok().body("TV permanently deleted");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: Failed to permanently delete TV");
        }
    }
    
    // Toggle TV status (admin only)
    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleTVStatus(@PathVariable("id") Long id) {
        try {
            TV tv = tvService.toggleTVStatus(id);
            return ResponseEntity.ok(tv);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: Failed to toggle TV status");
        }
    }
    
    // Search TVs by display name (admin only)
    @GetMapping("/search/display-name")
    public ResponseEntity<List<TV>> searchTVsByDisplayName(@RequestParam("displayName") String displayName) {
        List<TV> tvs = tvService.searchTVsByDisplayName(displayName);
        return ResponseEntity.ok(tvs);
    }
    
    // Search TVs by location (admin only)
    @GetMapping("/search/location")
    public ResponseEntity<List<TV>> searchTVsByLocation(@RequestParam("location") String location) {
        List<TV> tvs = tvService.searchTVsByLocation(location);
        return ResponseEntity.ok(tvs);
    }
    
    // Check if TV is active by name (public access)
    @GetMapping("/check/{name}")
    public ResponseEntity<?> checkTVStatus(@PathVariable("name") String name) {
        try {
            boolean isActive = tvService.isTVActiveByName(name);
            return ResponseEntity.ok().body("{\"active\": " + isActive + "}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // Initialize default TVs (admin only - for migration)
    @PostMapping("/initialize-defaults")
    public ResponseEntity<?> initializeDefaultTVs() {
        try {
            tvService.initializeDefaultTVs();
            return ResponseEntity.ok().body("Default TVs initialized successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
