package org.hyper.notificationbackend.controllers;

import org.hyper.notificationbackend.models.TVEnum;
import org.hyper.notificationbackend.services.TVEnumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/tv")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TVController {

    @Autowired
    private TVEnumService tvEnumService;
    
    // Get all TVs
    @GetMapping("/all")
    public ResponseEntity<List<TVEnum>> getAllTVs() {
        return ResponseEntity.ok(tvEnumService.getAllTVs());
    }
    
    // Get active TVs
    @GetMapping("/active")
    public ResponseEntity<List<TVEnum>> getActiveTVs() {
        return ResponseEntity.ok(tvEnumService.getActiveTVs());
    }
    
    // Get TV by name
    @GetMapping("/{name}")
    public ResponseEntity<?> getTVByName(@PathVariable String name) {
        Optional<TVEnum> tv = tvEnumService.getTVByName(name);
        return tv.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Update TV state
    @PutMapping("/{name}/state")
    public ResponseEntity<?> updateTVState(@PathVariable String name, @RequestBody Map<String, String> request) {
        try {
            Optional<TVEnum> tvOpt = tvEnumService.getTVByName(name);
            if (tvOpt.isPresent()) {
                TVEnum.TVState newState = TVEnum.TVState.valueOf(request.get("state"));
                TVEnum updatedTV = tvEnumService.updateTVState(tvOpt.get(), newState);
                return ResponseEntity.ok(updatedTV);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // Update TV active status
    @PutMapping("/{name}/active")
    public ResponseEntity<?> updateTVActiveStatus(@PathVariable String name, @RequestBody Map<String, Boolean> request) {
        try {
            Optional<TVEnum> tvOpt = tvEnumService.getTVByName(name);
            if (tvOpt.isPresent()) {
                boolean active = request.get("active");
                TVEnum updatedTV = tvEnumService.updateTVActiveStatus(tvOpt.get(), active);
                return ResponseEntity.ok(updatedTV);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
