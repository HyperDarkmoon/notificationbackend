package org.hyper.notificationbackend.services;

import org.hyper.notificationbackend.models.ContentSchedule;
import org.hyper.notificationbackend.models.TVEnum;
import org.hyper.notificationbackend.repositories.ContentScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ContentScheduleService {
    
    @Autowired
    private ContentScheduleRepository contentScheduleRepository;
    
    // Create a new content schedule
    public ContentSchedule createSchedule(ContentSchedule contentSchedule) {
        validateSchedule(contentSchedule);
        return contentScheduleRepository.save(contentSchedule);
    }
    
    // Get all content schedules
    public List<ContentSchedule> getAllSchedules() {
        return contentScheduleRepository.findAll();
    }
    
    // Get content schedule by ID
    public Optional<ContentSchedule> getScheduleById(Long id) {
        return contentScheduleRepository.findById(id);
    }
    
    // Get currently active schedules (includes both time-based and immediate schedules)
    public List<ContentSchedule> getCurrentlyActiveSchedules() {
        List<ContentSchedule> activeSchedules = contentScheduleRepository.findCurrentlyActive(LocalDateTime.now());
        List<ContentSchedule> immediateSchedules = contentScheduleRepository.findImmediateSchedules();
        activeSchedules.addAll(immediateSchedules);
        return activeSchedules;
    }
    
    // Get upcoming schedules
    public List<ContentSchedule> getUpcomingSchedules() {
        return contentScheduleRepository.findUpcoming(LocalDateTime.now());
    }
    
    // Get immediate/indefinite schedules
    public List<ContentSchedule> getImmediateSchedules() {
        return contentScheduleRepository.findImmediateSchedules();
    }
    
    // Get schedules for a specific TV
    public List<ContentSchedule> getSchedulesForTV(TVEnum tv) {
        return contentScheduleRepository.findByTV(tv);
    }
    
    // Get upcoming schedules for a specific TV
    public List<ContentSchedule> getUpcomingSchedulesForTV(TVEnum tv) {
        return contentScheduleRepository.findUpcomingForTV(tv, LocalDateTime.now());
    }
    
    // Update a content schedule
    public ContentSchedule updateSchedule(Long id, ContentSchedule updatedSchedule) {
        Optional<ContentSchedule> existingScheduleOpt = contentScheduleRepository.findById(id);
        if (existingScheduleOpt.isPresent()) {
            validateSchedule(updatedSchedule);
            ContentSchedule existingSchedule = existingScheduleOpt.get();
            
            // Update fields
            existingSchedule.setTitle(updatedSchedule.getTitle());
            existingSchedule.setDescription(updatedSchedule.getDescription());
            existingSchedule.setContentType(updatedSchedule.getContentType());
            existingSchedule.setContent(updatedSchedule.getContent());
            existingSchedule.setImageUrls(updatedSchedule.getImageUrls());
            existingSchedule.setVideoUrls(updatedSchedule.getVideoUrls());
            existingSchedule.setStartTime(updatedSchedule.getStartTime());
            existingSchedule.setEndTime(updatedSchedule.getEndTime());
            existingSchedule.setActive(updatedSchedule.isActive());
            existingSchedule.setTargetTVs(updatedSchedule.getTargetTVs());
            
            return contentScheduleRepository.save(existingSchedule);
        }
        throw new RuntimeException("Content schedule not found with id: " + id);
    }
    
    // Delete a content schedule
    public void deleteSchedule(Long id) {
        contentScheduleRepository.deleteById(id);
    }
    
    // Validate schedule data
    private void validateSchedule(ContentSchedule schedule) {
        // Only validate time relationship if both start and end times are provided
        if (schedule.getStartTime() != null && schedule.getEndTime() != null) {
            if (schedule.getStartTime().isAfter(schedule.getEndTime())) {
                throw new IllegalArgumentException("Start time must be before end time");
            }
        }
        // If only one time is provided, throw an error
        else if ((schedule.getStartTime() != null && schedule.getEndTime() == null) ||
                 (schedule.getStartTime() == null && schedule.getEndTime() != null)) {
            throw new IllegalArgumentException("Both start time and end time must be provided, or both must be null for immediate/indefinite content");
        }
        
        if (schedule.getContentType() == null) {
            throw new IllegalArgumentException("Content type must be provided");
        }
        
        // Validate image URLs based on content type
        if (schedule.getContentType() == ContentSchedule.ContentType.IMAGE_SINGLE && 
            (schedule.getImageUrls() == null || schedule.getImageUrls().size() != 1)) {
            throw new IllegalArgumentException("Single image content type requires exactly one image URL");
        } else if (schedule.getContentType() == ContentSchedule.ContentType.IMAGE_DUAL && 
                  (schedule.getImageUrls() == null || schedule.getImageUrls().size() != 2)) {
            throw new IllegalArgumentException("Dual image content type requires exactly two image URLs");
        } else if (schedule.getContentType() == ContentSchedule.ContentType.IMAGE_QUAD && 
                  (schedule.getImageUrls() == null || schedule.getImageUrls().size() != 4)) {
            throw new IllegalArgumentException("Quad image content type requires exactly four image URLs");
        } else if (schedule.getContentType() == ContentSchedule.ContentType.VIDEO && 
                  (schedule.getVideoUrls() == null || schedule.getVideoUrls().size() != 1)) {
            throw new IllegalArgumentException("Video content type requires exactly one video URL");
        } else if (schedule.getContentType() == ContentSchedule.ContentType.EMBED && 
                  (schedule.getContent() == null || schedule.getContent().isEmpty())) {
            throw new IllegalArgumentException("Embed content type requires embed content");
        } else if (schedule.getContentType() == ContentSchedule.ContentType.TEXT &&
                  (schedule.getContent() == null || schedule.getContent().isEmpty())) {
            throw new IllegalArgumentException("Text content type requires text content");
        }
        
        if (schedule.getTargetTVs() == null || schedule.getTargetTVs().isEmpty()) {
            throw new IllegalArgumentException("At least one target TV must be specified");
        }
    }
}
