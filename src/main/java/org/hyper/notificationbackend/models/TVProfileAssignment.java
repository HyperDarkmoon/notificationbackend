package org.hyper.notificationbackend.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tv_profile_assignments")
public class TVProfileAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tv_id", nullable = false)
    private TV tv;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "profile_id", nullable = false)
    private TVProfile profile;
    
    @Column(nullable = false)
    private LocalDateTime assignedAt;
    
    @Column(nullable = false)
    private boolean active = true;
    
    // Constructors
    public TVProfileAssignment() {
        this.assignedAt = LocalDateTime.now();
    }
    
    public TVProfileAssignment(TV tv, TVProfile profile) {
        this();
        this.tv = tv;
        this.profile = profile;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public TV getTv() {
        return tv;
    }
    
    public void setTv(TV tv) {
        this.tv = tv;
    }
    
    public TVProfile getProfile() {
        return profile;
    }
    
    public void setProfile(TVProfile profile) {
        this.profile = profile;
    }
    
    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }
    
    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
}
