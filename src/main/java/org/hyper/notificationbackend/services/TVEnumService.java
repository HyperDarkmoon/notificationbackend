package org.hyper.notificationbackend.services;

import org.hyper.notificationbackend.models.TVEnum;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TVEnumService {
    
    // Get all TVs
    public List<TVEnum> getAllTVs() {
        return Arrays.asList(TVEnum.values());
    }
    
    // Get active TVs
    public List<TVEnum> getActiveTVs() {
        return Arrays.stream(TVEnum.values())
                .filter(TVEnum::isActive)
                .collect(Collectors.toList());
    }
    
    // Get TV by its enum value
    public TVEnum getTV(TVEnum tv) {
        return tv;
    }
    
    // Get TV by name
    public Optional<TVEnum> getTVByName(String name) {
        return Arrays.stream(TVEnum.values())
                .filter(tv -> tv.name().equals(name) || tv.getDisplayName().equals(name))
                .findFirst();
    }
    
    // Update TV state
    public TVEnum updateTVState(TVEnum tv, TVEnum.TVState newState) {
        tv.setCurrentState(newState);
        return tv;
    }
    
    // Update TV active status
    public TVEnum updateTVActiveStatus(TVEnum tv, boolean active) {
        tv.setActive(active);
        return tv;
    }
}
