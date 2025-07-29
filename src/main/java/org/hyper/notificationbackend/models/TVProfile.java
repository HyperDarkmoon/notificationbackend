package org.hyper.notificationbackend.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "tv_profiles")
public class TVProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 255)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference
    @OrderBy("slideOrder ASC")
    private List<ProfileSlide> slides = new ArrayList<>();
    
    // Scheduling fields
    @OneToMany(mappedBy = "tvProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference
    private List<ProfileTimeSchedule> timeSchedules = new ArrayList<>();
    
    @Column(name = "is_immediate")
    private boolean isImmediate = false; // false means it's scheduled
    
    // Daily schedule fields
    @Column(name = "is_daily_schedule")
    private Boolean dailySchedule = false;
    
    @Column(name = "daily_start_time")
    private String dailyStartTime; // Format: "HH:MM"
    
    @Column(name = "daily_end_time")
    private String dailyEndTime; // Format: "HH:MM"
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(nullable = false)
    private boolean active = true;
    
    // Constructors
    public TVProfile() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public TVProfile(String name, String description) {
        this();
        this.name = name;
        this.description = description;
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
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }
    
    public List<ProfileSlide> getSlides() {
        return slides;
    }
    
    public void setSlides(List<ProfileSlide> slides) {
        this.slides = slides;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
        this.updatedAt = LocalDateTime.now();
    }
    
    public List<ProfileTimeSchedule> getTimeSchedules() {
        return timeSchedules;
    }
    
    public void setTimeSchedules(List<ProfileTimeSchedule> timeSchedules) {
        this.timeSchedules = timeSchedules;
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean isImmediate() {
        return isImmediate;
    }
    
    public void setImmediate(boolean isImmediate) {
        this.isImmediate = isImmediate;
        this.updatedAt = LocalDateTime.now();
    }
    
    // Daily schedule getters and setters
    public boolean isDailySchedule() {
        return dailySchedule != null ? dailySchedule : false;
    }
    
    public void setDailySchedule(Boolean dailySchedule) {
        this.dailySchedule = dailySchedule;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getDailyStartTime() {
        return dailyStartTime;
    }
    
    public void setDailyStartTime(String dailyStartTime) {
        this.dailyStartTime = dailyStartTime;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getDailyEndTime() {
        return dailyEndTime;
    }
    
    public void setDailyEndTime(String dailyEndTime) {
        this.dailyEndTime = dailyEndTime;
        this.updatedAt = LocalDateTime.now();
    }
    
    // Helper methods
    public void addSlide(ProfileSlide slide) {
        slides.add(slide);
        slide.setProfile(this);
        this.updatedAt = LocalDateTime.now();
    }
    
    public void removeSlide(ProfileSlide slide) {
        slides.remove(slide);
        slide.setProfile(null);
        this.updatedAt = LocalDateTime.now();
    }
    
    public void addTimeSchedule(ProfileTimeSchedule timeSchedule) {
        timeSchedules.add(timeSchedule);
        timeSchedule.setTvProfile(this);
        this.updatedAt = LocalDateTime.now();
    }
    
    public void removeTimeSchedule(ProfileTimeSchedule timeSchedule) {
        timeSchedules.remove(timeSchedule);
        timeSchedule.setTvProfile(null);
        this.updatedAt = LocalDateTime.now();
    }
    
    public void clearSlides() {
        slides.clear();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Helper method to check if daily schedule is currently active
    public boolean isDailyScheduleActive(LocalDateTime currentTime) {
        if (dailySchedule == null || !dailySchedule || dailyStartTime == null || dailyEndTime == null) {
            return false;
        }
        
        try {
            // Parse the daily start and end times
            String[] startParts = dailyStartTime.split(":");
            String[] endParts = dailyEndTime.split(":");
            
            int startHour = Integer.parseInt(startParts[0]);
            int startMinute = Integer.parseInt(startParts[1]);
            int endHour = Integer.parseInt(endParts[0]);
            int endMinute = Integer.parseInt(endParts[1]);
            
            // Get current time components
            int currentHour = currentTime.getHour();
            int currentMinute = currentTime.getMinute();
            
            // Convert to minutes for easier comparison
            int startTimeMinutes = startHour * 60 + startMinute;
            int endTimeMinutes = endHour * 60 + endMinute;
            int currentTimeMinutes = currentHour * 60 + currentMinute;
            
            // Handle overnight schedules (e.g., 23:00 to 01:00)
            if (endTimeMinutes <= startTimeMinutes) {
                // Overnight schedule
                return currentTimeMinutes >= startTimeMinutes || currentTimeMinutes <= endTimeMinutes;
            } else {
                // Same day schedule
                return currentTimeMinutes >= startTimeMinutes && currentTimeMinutes <= endTimeMinutes;
            }
        } catch (Exception e) {
            // If parsing fails, return false
            return false;
        }
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
