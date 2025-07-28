package org.hyper.notificationbackend.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tv_profile_assignments")
public class TVProfileAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tv_name", nullable = false)
    private TVEnum tvName;
    
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
    
    public TVProfileAssignment(TVEnum tvName, TVProfile profile) {
        this();
        this.tvName = tvName;
        this.profile = profile;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public TVEnum getTvName() {
        return tvName;
    }
    
    public void setTvName(TVEnum tvName) {
        this.tvName = tvName;
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
