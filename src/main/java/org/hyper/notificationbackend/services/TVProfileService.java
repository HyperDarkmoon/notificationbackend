package org.hyper.notificationbackend.services;

import org.hyper.notificationbackend.models.TVProfile;
import org.hyper.notificationbackend.models.ProfileSlide;
import org.hyper.notificationbackend.repositories.TVProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TVProfileService {
    
    @Autowired
    private TVProfileRepository tvProfileRepository;
    
    // Create a new profile
    public TVProfile createProfile(TVProfile profile) {
        validateProfile(profile);
        return tvProfileRepository.save(profile);
    }
    
    // Get all active profiles
    public List<TVProfile> getAllProfiles() {
        return tvProfileRepository.findAllActiveWithSlides();
    }
    
    // Get profile by ID
    public Optional<TVProfile> getProfileById(Long id) {
        return tvProfileRepository.findByIdWithSlides(id);
    }
    
    // Update profile
    public TVProfile updateProfile(Long id, TVProfile updatedProfile) {
        Optional<TVProfile> existingProfile = tvProfileRepository.findById(id);
        if (existingProfile.isPresent()) {
            TVProfile profile = existingProfile.get();
            profile.setName(updatedProfile.getName());
            profile.setDescription(updatedProfile.getDescription());
            profile.setActive(updatedProfile.isActive());
            
            // Update slides
            profile.clearSlides();
            if (updatedProfile.getSlides() != null) {
                for (ProfileSlide slide : updatedProfile.getSlides()) {
                    slide.setProfile(profile);
                    profile.addSlide(slide);
                }
            }
            
            validateProfile(profile);
            return tvProfileRepository.save(profile);
        }
        throw new RuntimeException("Profile not found with id: " + id);
    }
    
    // Delete profile (soft delete)
    public void deleteProfile(Long id) {
        Optional<TVProfile> profile = tvProfileRepository.findById(id);
        if (profile.isPresent()) {
            TVProfile p = profile.get();
            p.setActive(false);
            tvProfileRepository.save(p);
        } else {
            throw new RuntimeException("Profile not found with id: " + id);
        }
    }
    
    // Search profiles by name
    public List<TVProfile> searchProfilesByName(String name) {
        return tvProfileRepository.findByNameContainingIgnoreCase(name);
    }
    
    // Validate profile
    private void validateProfile(TVProfile profile) {
        if (profile.getName() == null || profile.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Profile name cannot be empty");
        }
        
        if (profile.getSlides() != null && profile.getSlides().size() > 3) {
            throw new IllegalArgumentException("Profile cannot have more than 3 slides");
        }
        
        // Validate slides
        if (profile.getSlides() != null) {
            for (int i = 0; i < profile.getSlides().size(); i++) {
                ProfileSlide slide = profile.getSlides().get(i);
                slide.setSlideOrder(i + 1); // Set order based on position
                validateSlide(slide);
            }
        }
    }
    
    // Validate individual slide
    private void validateSlide(ProfileSlide slide) {
        if (slide.getTitle() == null || slide.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Slide title cannot be empty");
        }
        
        if (slide.getContentType() == null) {
            throw new IllegalArgumentException("Slide content type cannot be null");
        }
        
        if (slide.getDurationSeconds() == null || slide.getDurationSeconds() < 1) {
            slide.setDurationSeconds(10); // Default to 10 seconds
        }
        
        // Validate content based on type
        switch (slide.getContentType()) {
            case TEXT:
                if (slide.getContent() == null || slide.getContent().trim().isEmpty()) {
                    throw new IllegalArgumentException("Text content cannot be empty for TEXT type slides");
                }
                break;
            case EMBED:
                if (slide.getContent() == null || slide.getContent().trim().isEmpty()) {
                    throw new IllegalArgumentException("Embed content cannot be empty for EMBED type slides");
                }
                break;
            case IMAGE_SINGLE:
                if (slide.getImageUrls() == null || slide.getImageUrls().isEmpty()) {
                    throw new IllegalArgumentException("At least one image URL is required for IMAGE_SINGLE type slides");
                }
                break;
            case IMAGE_DUAL:
                if (slide.getImageUrls() == null || slide.getImageUrls().size() < 2) {
                    throw new IllegalArgumentException("At least two image URLs are required for IMAGE_DUAL type slides");
                }
                break;
            case IMAGE_QUAD:
                if (slide.getImageUrls() == null || slide.getImageUrls().size() < 4) {
                    throw new IllegalArgumentException("At least four image URLs are required for IMAGE_QUAD type slides");
                }
                break;
            case VIDEO:
                if (slide.getVideoUrls() == null || slide.getVideoUrls().isEmpty()) {
                    throw new IllegalArgumentException("At least one video URL is required for VIDEO type slides");
                }
                break;
        }
    }
}
