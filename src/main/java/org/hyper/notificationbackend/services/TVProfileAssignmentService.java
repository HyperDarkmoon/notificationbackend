package org.hyper.notificationbackend.services;

import org.hyper.notificationbackend.models.TVProfile;
import org.hyper.notificationbackend.models.TVProfileAssignment;
import org.hyper.notificationbackend.models.TVEnum;
import org.hyper.notificationbackend.repositories.TVProfileAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TVProfileAssignmentService {
    
    @Autowired
    private TVProfileAssignmentRepository assignmentRepository;
    
    @Autowired
    private TVProfileService profileService;
    
    // Assign a profile to a TV
    @Transactional
    public TVProfileAssignment assignProfileToTV(TVEnum tvName, Long profileId) {
        // Get the profile
        Optional<TVProfile> profile = profileService.getProfileById(profileId);
        if (!profile.isPresent()) {
            throw new RuntimeException("Profile not found with id: " + profileId);
        }
        
        // Deactivate any existing assignments for this TV
        assignmentRepository.deactivateAssignmentsForTV(tvName);
        
        // Create new assignment
        TVProfileAssignment assignment = new TVProfileAssignment(tvName, profile.get());
        return assignmentRepository.save(assignment);
    }
    
    // Get current profile assignment for a TV
    public Optional<TVProfileAssignment> getCurrentAssignmentForTV(TVEnum tvName) {
        return assignmentRepository.findByTvNameWithProfile(tvName);
    }
    
    // Get current profile for a TV (just the profile data)
    public Optional<TVProfile> getCurrentProfileForTV(TVEnum tvName) {
        Optional<TVProfileAssignment> assignment = getCurrentAssignmentForTV(tvName);
        return assignment.map(TVProfileAssignment::getProfile);
    }
    
    // Remove profile assignment from TV
    public void removeProfileFromTV(TVEnum tvName) {
        assignmentRepository.deactivateAssignmentsForTV(tvName);
    }
    
    // Get all active assignments
    public List<TVProfileAssignment> getAllActiveAssignments() {
        return assignmentRepository.findByActiveTrue();
    }
    
    // Get all assignments for a specific profile
    public List<TVProfileAssignment> getAssignmentsForProfile(Long profileId) {
        return assignmentRepository.findByProfileIdAndActiveTrue(profileId);
    }
    
    // Check if a profile is currently assigned to any TV
    public boolean isProfileAssigned(Long profileId) {
        List<TVProfileAssignment> assignments = getAssignmentsForProfile(profileId);
        return !assignments.isEmpty();
    }
    
    // Delete assignment by ID
    public void deleteAssignment(Long assignmentId) {
        Optional<TVProfileAssignment> assignment = assignmentRepository.findById(assignmentId);
        if (assignment.isPresent()) {
            TVProfileAssignment assign = assignment.get();
            assign.setActive(false);
            assignmentRepository.save(assign);
        } else {
            throw new RuntimeException("Assignment not found with id: " + assignmentId);
        }
    }
}
