package org.hyper.notificationbackend.services;

import org.hyper.notificationbackend.models.TV;
import org.hyper.notificationbackend.models.User;
import org.hyper.notificationbackend.repositories.TVRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TVService {

    @Autowired
    private TVRepository tvRepository;

    public TV createTV(String name, String location, String description, User owner) {
        TV tv = new TV(name, location, owner);
        if (description != null) {
            tv.setDescription(description);
        }
        return tvRepository.save(tv);
    }

    public List<TV> getTVsByOwner(User owner) {
        return tvRepository.findByOwner(owner);
    }

    public List<TV> getActiveTVsByOwner(User owner) {
        return tvRepository.findByOwnerAndIsActive(owner, true);
    }

    public List<TV> getAllActiveTVs() {
        return tvRepository.findByIsActive(true);
    }

    public Optional<TV> findById(Long id) {
        return tvRepository.findById(id);
    }

    public TV updateTV(Long id, String name, String location, String description) {
        Optional<TV> tvOpt = tvRepository.findById(id);
        if (tvOpt.isPresent()) {
            TV tv = tvOpt.get();
            if (name != null) tv.setName(name);
            if (location != null) tv.setLocation(location);
            if (description != null) tv.setDescription(description);
            return tvRepository.save(tv);
        }
        throw new RuntimeException("TV not found with id: " + id);
    }

    public TV toggleTVStatus(Long id) {
        Optional<TV> tvOpt = tvRepository.findById(id);
        if (tvOpt.isPresent()) {
            TV tv = tvOpt.get();
            tv.setIsActive(!tv.getIsActive());
            return tvRepository.save(tv);
        }
        throw new RuntimeException("TV not found with id: " + id);
    }

    public void deleteTV(Long id) {
        tvRepository.deleteById(id);
    }

    public List<TV> getAllTVs() {
        return tvRepository.findAll();
    }
}
