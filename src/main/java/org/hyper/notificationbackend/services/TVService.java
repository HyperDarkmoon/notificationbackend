package org.hyper.notificationbackend.services;

import org.hyper.notificationbackend.models.TV;
import org.hyper.notificationbackend.repositories.TVRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TVService {
    
    @Autowired
    private TVRepository tvRepository;
    
    // Initialize default TVs if none exist
    public void initializeDefaultTVs() {
        if (tvRepository.count() == 0) {
            TV tv1 = new TV("TV 1", "Reception");
            TV tv2 = new TV("TV 2", "Cafeteria");
            TV tv3 = new TV("TV 3", "Meeting Room");
            TV tv4 = new TV("TV 4", "Lobby");
            
            tvRepository.save(tv1);
            tvRepository.save(tv2);
            tvRepository.save(tv3);
            tvRepository.save(tv4);
        }
    }
    
    // Get all TVs
    public List<TV> getAllTVs() {
        return tvRepository.findAll();
    }
    
    // Get active TVs
    public List<TV> getActiveTVs() {
        return tvRepository.findByActive(true);
    }
    
    // Get TV by ID
    public Optional<TV> getTVById(Long id) {
        return tvRepository.findById(id);
    }
    
    // Update TV state
    public TV updateTVState(Long id, TV.TVState newState) {
        Optional<TV> tvOpt = tvRepository.findById(id);
        if (tvOpt.isPresent()) {
            TV tv = tvOpt.get();
            tv.setCurrentState(newState);
            return tvRepository.save(tv);
        }
        throw new RuntimeException("TV not found with id: " + id);
    }
    
    // Update TV active status
    public TV updateTVActiveStatus(Long id, boolean active) {
        Optional<TV> tvOpt = tvRepository.findById(id);
        if (tvOpt.isPresent()) {
            TV tv = tvOpt.get();
            tv.setActive(active);
            return tvRepository.save(tv);
        }
        throw new RuntimeException("TV not found with id: " + id);
    }
    
    // Save or update TV
    public TV saveOrUpdateTV(TV tv) {
        return tvRepository.save(tv);
    }
}
