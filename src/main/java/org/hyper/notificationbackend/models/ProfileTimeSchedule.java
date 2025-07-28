package org.hyper.notificationbackend.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "profile_time_schedules")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ProfileTimeSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;
    
    @Column(name = "active")
    private boolean active = true;
    
    // Many-to-one relationship with TVProfile
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tv_profile_id", nullable = false)
    @JsonBackReference
    private TVProfile tvProfile;
    
    // Constructors
    public ProfileTimeSchedule() {}
    
    public ProfileTimeSchedule(LocalDateTime startTime, LocalDateTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    public ProfileTimeSchedule(LocalDateTime startTime, LocalDateTime endTime, TVProfile tvProfile) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.tvProfile = tvProfile;
    }
    
    // Helper methods
    public boolean isCurrentlyActive(LocalDateTime currentTime) {
        return active && currentTime.isAfter(startTime) && currentTime.isBefore(endTime);
    }
    
    public boolean isExpired(LocalDateTime currentTime) {
        return currentTime.isAfter(endTime);
    }
    
    public boolean isUpcoming(LocalDateTime currentTime) {
        return currentTime.isBefore(startTime);
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public TVProfile getTvProfile() {
        return tvProfile;
    }
    
    public void setTvProfile(TVProfile tvProfile) {
        this.tvProfile = tvProfile;
    }
    
    @Override
    public String toString() {
        return "ProfileTimeSchedule{" +
                "id=" + id +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", active=" + active +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        ProfileTimeSchedule that = (ProfileTimeSchedule) o;
        
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (startTime != null ? !startTime.equals(that.startTime) : that.startTime != null) return false;
        return endTime != null ? endTime.equals(that.endTime) : that.endTime == null;
    }
    
    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
        result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
        return result;
    }
}
