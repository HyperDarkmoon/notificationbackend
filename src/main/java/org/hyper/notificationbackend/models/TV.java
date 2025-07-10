package org.hyper.notificationbackend.models;

import jakarta.persistence.*;

@Entity
@Table(name = "tvs")
public class TV {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String location;
    private boolean active;
    
    // Current state tracking
    @Enumerated(EnumType.STRING)
    private TVState currentState = TVState.STATIC;
    
    public enum TVState {
        STATIC,  // State 1: showing static data
        TEXT,    // State 2: showing text
        CUSTOM   // State 3: showing custom content
    }
    
    // Constructors
    public TV() {}
    
    public TV(String name, String location) {
        this.name = name;
        this.location = location;
        this.active = true;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public TVState getCurrentState() {
        return currentState;
    }
    
    public void setCurrentState(TVState currentState) {
        this.currentState = currentState;
    }
}
