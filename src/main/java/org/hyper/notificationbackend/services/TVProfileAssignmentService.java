package org.hyper.notificationbackend.services;

import org.hyper.notificationbackend.models.TVProfile;
import org.hyper.notificationbackend.models.TVProfileAssignment;
import org.hyper.notificationbackend.models.TV;
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
    
    @Autowired
    private TVService tvService;
    
    // Helper method to convert TVEnum to TV entity (for backward compatibility)
    private Optional<TV> convertTVEnumToEntity(TVEnum tvEnum) {
        return tvService.getActiveTVByName(tvEnum.name());
    }
    
    // Assign a profile to a TV - TVEnum version for backward compatibility
    @Transactional
    public TVProfileAssignment assignProfileToTV(TVEnum tvEnum, Long profileId) {
        Optional<TV> tvOpt = convertTVEnumToEntity(tvEnum);
        if (tvOpt.isEmpty()) {
            throw new RuntimeException("TV not found: " + tvEnum.name());
        }
        return assignProfileToTV(tvOpt.get(), profileId);
    }
    
    // Assign a profile to a TV - TV entity version
    @Transactional
    public TVProfileAssignment assignProfileToTV(TV tv, Long profileId) {
        // Get the profile
        Optional<TVProfile> profile = profileService.getProfileById(profileId);
        if (!profile.isPresent()) {
            throw new RuntimeException("Profile not found with id: " + profileId);
        }
        
        // Deactivate any existing assignments for this TV
        assignmentRepository.deactivateAssignmentsForTV(tv);
        
        // Create new assignment
        TVProfileAssignment assignment = new TVProfileAssignment(tv, profile.get());
        return assignmentRepository.save(assignment);
    }
    
    // Get current profile assignment for a TV - TVEnum version for backward compatibility
    public Optional<TVProfileAssignment> getCurrentAssignmentForTV(TVEnum tvEnum) {
        Optional<TV> tvOpt = convertTVEnumToEntity(tvEnum);
        if (tvOpt.isEmpty()) {
            return Optional.empty();
        }
        return getCurrentAssignmentForTV(tvOpt.get());
    }
    
    // Get current profile assignment for a TV - TV entity version
    public Optional<TVProfileAssignment> getCurrentAssignmentForTV(TV tv) {
        return assignmentRepository.findByTvWithProfile(tv);
    }
    
    // Get current profile for a TV (just the profile data) - TVEnum version for backward compatibility
    public Optional<TVProfile> getCurrentProfileForTV(TVEnum tvEnum) {
        Optional<TVProfileAssignment> assignment = getCurrentAssignmentForTV(tvEnum);
        return assignment.map(TVProfileAssignment::getProfile);
    }
    
    // Get current profile for a TV (just the profile data) - TV entity version
    public Optional<TVProfile> getCurrentProfileForTV(TV tv) {
        Optional<TVProfileAssignment> assignment = getCurrentAssignmentForTV(tv);
        return assignment.map(TVProfileAssignment::getProfile);
    }
    
    // Remove profile assignment from TV - TVEnum version for backward compatibility
    public void removeProfileFromTV(TVEnum tvEnum) {
        Optional<TV> tvOpt = convertTVEnumToEntity(tvEnum);
        if (tvOpt.isPresent()) {
            removeProfileFromTV(tvOpt.get());
        }
    }
    
    // Remove profile assignment from TV - TV entity version
    public void removeProfileFromTV(TV tv) {
        assignmentRepository.deactivateAssignmentsForTV(tv);
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
