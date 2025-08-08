package org.hyper.notificationbackend.services;

import org.hyper.notificationbackend.models.TV;
import org.hyper.notificationbackend.repositories.TVRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TVService {
    
    @Autowired
    private TVRepository tvRepository;
    
    // Create a new TV
    public TV createTV(TV tv) {
        validateTV(tv);
        
        // Check if TV name already exists
        if (tvRepository.existsByNameIgnoreCase(tv.getName())) {
            throw new IllegalArgumentException("TV with name '" + tv.getName() + "' already exists");
        }
        
        return tvRepository.save(tv);
    }
    
    // Get all TVs
    public List<TV> getAllTVs() {
        return tvRepository.findAllOrderByName();
    }
    
    // Get all active TVs
    public List<TV> getAllActiveTVs() {
        return tvRepository.findAllActiveOrderByName();
    }
    
    // Get TV by ID
    public Optional<TV> getTVById(Long id) {
        return tvRepository.findById(id);
    }
    
    // Get TV by name
    public Optional<TV> getTVByName(String name) {
        return tvRepository.findByNameIgnoreCase(name);
    }
    
    // Get active TV by name
    public Optional<TV> getActiveTVByName(String name) {
        return tvRepository.findByNameIgnoreCaseAndActiveTrue(name);
    }
    
    // Update TV
    public TV updateTV(Long id, TV updatedTV) {
        Optional<TV> existingTVOpt = tvRepository.findById(id);
        if (!existingTVOpt.isPresent()) {
            throw new IllegalArgumentException("TV not found with ID: " + id);
        }
        
        TV existingTV = existingTVOpt.get();
        
        // Validate the updated TV
        validateTV(updatedTV);
        
        // Check if name is being changed and if new name already exists
        if (!existingTV.getName().equalsIgnoreCase(updatedTV.getName())) {
            if (tvRepository.existsByNameIgnoreCaseAndIdNot(updatedTV.getName(), id)) {
                throw new IllegalArgumentException("TV with name '" + updatedTV.getName() + "' already exists");
            }
        }
        
        // Update fields
        existingTV.setName(updatedTV.getName());
        existingTV.setDisplayName(updatedTV.getDisplayName());
        existingTV.setDescription(updatedTV.getDescription());
        existingTV.setLocation(updatedTV.getLocation());
        existingTV.setActive(updatedTV.isActive());
        
        return tvRepository.save(existingTV);
    }
    
    // Delete TV (soft delete by setting active to false)
    public void deleteTV(Long id) {
        Optional<TV> tvOpt = tvRepository.findById(id);
        if (!tvOpt.isPresent()) {
            throw new IllegalArgumentException("TV not found with ID: " + id);
        }
        
        TV tv = tvOpt.get();
        tv.setActive(false);
        tvRepository.save(tv);
    }
    
    // Permanently delete TV (use with caution)
    public void permanentlyDeleteTV(Long id) {
        if (!tvRepository.existsById(id)) {
            throw new IllegalArgumentException("TV not found with ID: " + id);
        }
        
        tvRepository.deleteById(id);
    }
    
    // Search TVs by display name
    public List<TV> searchTVsByDisplayName(String displayName) {
        return tvRepository.findByDisplayNameContainingIgnoreCase(displayName);
    }
    
    // Search TVs by location
    public List<TV> searchTVsByLocation(String location) {
        return tvRepository.findByLocationContainingIgnoreCase(location);
    }
    
    // Toggle TV active status
    public TV toggleTVStatus(Long id) {
        Optional<TV> tvOpt = tvRepository.findById(id);
        if (!tvOpt.isPresent()) {
            throw new IllegalArgumentException("TV not found with ID: " + id);
        }
        
        TV tv = tvOpt.get();
        tv.setActive(!tv.isActive());
        return tvRepository.save(tv);
    }
    
    // Check if TV exists and is active
    public boolean isTVActiveById(Long id) {
        Optional<TV> tvOpt = tvRepository.findById(id);
        return tvOpt.isPresent() && tvOpt.get().isActive();
    }
    
    // Check if TV exists and is active by name
    public boolean isTVActiveByName(String name) {
        Optional<TV> tvOpt = tvRepository.findByNameIgnoreCaseAndActiveTrue(name);
        return tvOpt.isPresent();
    }
    
    // Validate TV
    private void validateTV(TV tv) {
        if (tv == null) {
            throw new IllegalArgumentException("TV cannot be null");
        }
        
        if (tv.getName() == null || tv.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("TV name cannot be empty");
        }
        
        if (tv.getDisplayName() == null || tv.getDisplayName().trim().isEmpty()) {
            throw new IllegalArgumentException("TV display name cannot be empty");
        }
        
        // Sanitize name - remove spaces and special characters, convert to uppercase
        String sanitizedName = tv.getName().trim().replaceAll("[^a-zA-Z0-9_]", "").toUpperCase();
        if (sanitizedName.isEmpty()) {
            throw new IllegalArgumentException("TV name must contain at least one alphanumeric character");
        }
        tv.setName(sanitizedName);
        
        // Trim other fields
        tv.setDisplayName(tv.getDisplayName().trim());
        if (tv.getDescription() != null) {
            tv.setDescription(tv.getDescription().trim());
        }
        if (tv.getLocation() != null) {
            tv.setLocation(tv.getLocation().trim());
        }
    }
    
    // Initialize default TVs (for migration from enum)
    @Transactional
    public void initializeDefaultTVs() {
        // Check if any TVs already exist
        if (tvRepository.count() > 0) {
            return; // TVs already exist, skip initialization
        }
        
        // Create default TVs matching the old enum values
        createTV(new TV("TV1", "TV 1", "Default TV 1", null));
        createTV(new TV("TV2", "TV 2", "Default TV 2", null));
        createTV(new TV("TV3", "TV 3", "Default TV 3", null));
        createTV(new TV("TV4", "TV 4", "Default TV 4", null));
    }
}
