package org.hyper.notificationbackend.controllers;

import org.hyper.notificationbackend.models.TVProfile;
import org.hyper.notificationbackend.models.TVProfileAssignment;
import org.hyper.notificationbackend.models.TVEnum;
import org.hyper.notificationbackend.services.TVProfileService;
import org.hyper.notificationbackend.services.TVProfileAssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/profiles")
public class TVProfileController {

    @Autowired
    private TVProfileService profileService;
    
    @Autowired
    private TVProfileAssignmentService assignmentService;
    
    // Create a new profile
    @PostMapping
    public ResponseEntity<?> createProfile(@RequestBody TVProfile profile) {
        try {
            TVProfile newProfile = profileService.createProfile(profile);
            return ResponseEntity.ok(newProfile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // Get all profiles
    @GetMapping
    public ResponseEntity<List<TVProfile>> getAllProfiles() {
        List<TVProfile> profiles = profileService.getAllProfiles();
        return ResponseEntity.ok(profiles);
    }
    
    // Get profile by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getProfileById(@PathVariable("id") Long id) {
        Optional<TVProfile> profile = profileService.getProfileById(id);
        return profile.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Update profile
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProfile(@PathVariable("id") Long id, @RequestBody TVProfile profile) {
        try {
            TVProfile updatedProfile = profileService.updateProfile(id, profile);
            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // Delete profile
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProfile(@PathVariable("id") Long id) {
        try {
            profileService.deleteProfile(id);
            return ResponseEntity.ok("Profile deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // Search profiles by name
    @GetMapping("/search")
    public ResponseEntity<List<TVProfile>> searchProfiles(@RequestParam("name") String name) {
        List<TVProfile> profiles = profileService.searchProfilesByName(name);
        return ResponseEntity.ok(profiles);
    }
    
    // Assign profile to TV
    @PostMapping("/assign")
    public ResponseEntity<?> assignProfileToTV(@RequestBody Map<String, Object> request) {
        try {
            String tvName = (String) request.get("tvName");
            Long profileId = Long.valueOf(request.get("profileId").toString());
            
            TVEnum tv = TVEnum.valueOf(tvName);
            TVProfileAssignment assignment = assignmentService.assignProfileToTV(tv, profileId);
            return ResponseEntity.ok(assignment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // Remove profile from TV
    @DeleteMapping("/assign/{tvName}")
    public ResponseEntity<?> removeProfileFromTV(@PathVariable("tvName") String tvName) {
        try {
            TVEnum tv = TVEnum.valueOf(tvName);
            assignmentService.removeProfileFromTV(tv);
            return ResponseEntity.ok("Profile removed from TV successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // Get current profile assignment for TV
    @GetMapping("/tv/{tvName}")
    public ResponseEntity<?> getCurrentProfileAssignmentForTV(@PathVariable("tvName") String tvName) {
        try {
            TVEnum tv = TVEnum.valueOf(tvName);
            Optional<TVProfileAssignment> assignment = assignmentService.getCurrentAssignmentForTV(tv);
            return assignment.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.ok(null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // Get all active assignments
    @GetMapping("/assignments")
    public ResponseEntity<List<TVProfileAssignment>> getAllAssignments() {
        List<TVProfileAssignment> assignments = assignmentService.getAllActiveAssignments();
        return ResponseEntity.ok(assignments);
    }
    
    // Get assignments for a specific profile
    @GetMapping("/{id}/assignments")
    public ResponseEntity<List<TVProfileAssignment>> getAssignmentsForProfile(@PathVariable("id") Long profileId) {
        List<TVProfileAssignment> assignments = assignmentService.getAssignmentsForProfile(profileId);
        return ResponseEntity.ok(assignments);
    }
    
    // Delete assignment by ID
    @DeleteMapping("/assignments/{id}")
    public ResponseEntity<?> deleteAssignment(@PathVariable("id") Long assignmentId) {
        try {
            assignmentService.deleteAssignment(assignmentId);
            return ResponseEntity.ok("Assignment deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
