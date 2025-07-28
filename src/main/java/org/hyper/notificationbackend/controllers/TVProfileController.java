package org.hyper.notificationbackend.controllers;

import org.hyper.notificationbackend.models.TVProfile;
import org.hyper.notificationbackend.models.TVProfileAssignment;
import org.hyper.notificationbackend.models.ProfileTimeSchedule;
import org.hyper.notificationbackend.models.TVEnum;
import org.hyper.notificationbackend.services.TVProfileService;
import org.hyper.notificationbackend.services.TVProfileAssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
            // Debug logging
            System.out.println("Creating profile:");
            System.out.println("- Name: " + profile.getName());
            System.out.println("- isImmediate: " + profile.isImmediate());
            System.out.println("- timeSchedules count: " + (profile.getTimeSchedules() != null ? profile.getTimeSchedules().size() : 0));
            
            if (profile.getTimeSchedules() != null) {
                for (ProfileTimeSchedule schedule : profile.getTimeSchedules()) {
                    System.out.println("- Schedule: " + schedule.getStartTime() + " to " + schedule.getEndTime());
                }
            }
            
            TVProfile newProfile = profileService.createProfile(profile);
            System.out.println("Profile created with ID: " + newProfile.getId() + ", isImmediate: " + newProfile.isImmediate());
            return ResponseEntity.ok(newProfile);
        } catch (Exception e) {
            System.err.println("Error creating profile: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // Get all profiles
    @GetMapping
    public ResponseEntity<?> getAllProfiles() {
        try {
            List<TVProfile> profiles = profileService.getAllProfiles();
            return ResponseEntity.ok(profiles);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching profiles: " + e.getMessage());
        }
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
            
            if (assignment.isPresent()) {
                TVProfileAssignment tvAssignment = assignment.get();
                TVProfile profile = tvAssignment.getProfile();
                
                // Debug logging
                System.out.println("Profile ID: " + profile.getId());
                System.out.println("Profile Name: " + profile.getName());
                System.out.println("Profile isImmediate: " + profile.isImmediate());
                System.out.println("Profile timeSchedules count: " + (profile.getTimeSchedules() != null ? profile.getTimeSchedules().size() : 0));
                
                if (profile.getTimeSchedules() != null) {
                    for (ProfileTimeSchedule schedule : profile.getTimeSchedules()) {
                        System.out.println("Schedule: " + schedule.getStartTime() + " to " + schedule.getEndTime() + " (active: " + schedule.isActive() + ")");
                    }
                }
                
                // Check if profile is currently active based on its schedule
                boolean isProfileCurrentlyActive = profileService.isProfileCurrentlyActive(profile.getId());
                System.out.println("Profile currently active: " + isProfileCurrentlyActive);
                
                // If profile is not currently active due to schedule, return null
                if (!isProfileCurrentlyActive) {
                    System.out.println("Profile not currently active, returning null");
                    return ResponseEntity.ok(null);
                }
                
                System.out.println("Profile is active, returning assignment");
                return ResponseEntity.ok(tvAssignment);
            } else {
                System.out.println("No assignment found for TV: " + tvName);
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            System.err.println("Error in getCurrentProfileAssignmentForTV: " + e.getMessage());
            e.printStackTrace();
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
    
    // Profile scheduling endpoints
    
    // Add time schedule to profile
    @PostMapping("/{id}/schedules")
    public ResponseEntity<?> addTimeScheduleToProfile(
            @PathVariable("id") Long profileId,
            @RequestBody Map<String, String> request) {
        try {
            LocalDateTime startTime = LocalDateTime.parse(request.get("startTime"));
            LocalDateTime endTime = LocalDateTime.parse(request.get("endTime"));
            
            ProfileTimeSchedule schedule = profileService.addTimeScheduleToProfile(profileId, startTime, endTime);
            return ResponseEntity.ok(schedule);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // Get all time schedules for a profile
    @GetMapping("/{id}/schedules")
    public ResponseEntity<?> getProfileTimeSchedules(@PathVariable("id") Long profileId) {
        try {
            List<ProfileTimeSchedule> schedules = profileService.getProfileTimeSchedules(profileId);
            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // Remove time schedule from profile
    @DeleteMapping("/{id}/schedules/{scheduleId}")
    public ResponseEntity<?> removeTimeScheduleFromProfile(
            @PathVariable("id") Long profileId,
            @PathVariable("scheduleId") Long scheduleId) {
        try {
            profileService.removeTimeScheduleFromProfile(profileId, scheduleId);
            return ResponseEntity.ok("Time schedule removed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // Check if profile is currently active (considering schedules)
    @GetMapping("/{id}/active-status")
    public ResponseEntity<?> getProfileActiveStatus(@PathVariable("id") Long profileId) {
        try {
            boolean isActive = profileService.isProfileCurrentlyActive(profileId);
            return ResponseEntity.ok(Map.of("active", isActive));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // Get all currently active profiles
    @GetMapping("/currently-active")
    public ResponseEntity<?> getCurrentlyActiveProfiles() {
        try {
            List<TVProfile> activeProfiles = profileService.getCurrentlyActiveProfiles();
            return ResponseEntity.ok(activeProfiles);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // Fix profiles with inconsistent scheduling states (has schedules but isImmediate=true)
    @PostMapping("/fix-scheduling")
    public ResponseEntity<?> fixProfileSchedulingInconsistencies() {
        try {
            List<TVProfile> allProfiles = profileService.getAllProfiles();
            int fixedCount = 0;
            
            for (TVProfile profile : allProfiles) {
                // If profile has time schedules but is marked as immediate, fix it
                if (profile.isImmediate() && profile.getTimeSchedules() != null && !profile.getTimeSchedules().isEmpty()) {
                    profile.setImmediate(false);
                    profileService.updateProfile(profile.getId(), profile);
                    fixedCount++;
                    System.out.println("Fixed profile " + profile.getId() + " (" + profile.getName() + ") - set isImmediate to false");
                }
            }
            
            return ResponseEntity.ok(Map.of("message", "Fixed " + fixedCount + " profiles with scheduling inconsistencies"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
