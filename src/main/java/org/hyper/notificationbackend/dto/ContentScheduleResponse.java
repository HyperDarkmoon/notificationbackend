package org.hyper.notificationbackend.dto;

import org.hyper.notificationbackend.models.ContentSchedule;
import org.hyper.notificationbackend.models.TV;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ContentScheduleResponse {
    private Long id;
    private String title;
    private String description;
    private String contentType;
    private String content;
    private List<String> imageUrls;
    private List<String> videoUrls;
    private boolean active;
    private boolean immediate;
    private Set<String> targetTVs;
    private List<TimeScheduleResponse> timeSchedules;
    
    // Legacy fields for backward compatibility
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public static class TimeScheduleResponse {
        private Long id;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private boolean active;

        // Constructors
        public TimeScheduleResponse() {}

        public TimeScheduleResponse(Long id, LocalDateTime startTime, LocalDateTime endTime, boolean active) {
            this.id = id;
            this.startTime = startTime;
            this.endTime = endTime;
            this.active = active;
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }

    // Constructors
    public ContentScheduleResponse() {}

    // Factory method to convert from entity
    public static ContentScheduleResponse fromEntity(ContentSchedule entity) {
        ContentScheduleResponse response = new ContentScheduleResponse();
        response.setId(entity.getId());
        response.setTitle(entity.getTitle());
        response.setDescription(entity.getDescription());
        response.setContentType(entity.getContentType() != null ? entity.getContentType().name() : null);
        response.setContent(entity.getContent());
        response.setImageUrls(entity.getImageUrls());
        response.setVideoUrls(entity.getVideoUrls());
        response.setActive(entity.isActive());
        response.setImmediate(entity.isImmediate());
        
        // Convert target TVs to strings
        if (entity.getTargetTVs() != null) {
            response.setTargetTVs(entity.getTargetTVs().stream()
                .map(TV::getName)
                .collect(Collectors.toSet()));
        }
        
        // Convert time schedules
        if (entity.getTimeSchedules() != null) {
            response.setTimeSchedules(entity.getTimeSchedules().stream()
                .map(ts -> new TimeScheduleResponse(ts.getId(), ts.getStartTime(), ts.getEndTime(), ts.isActive()))
                .collect(Collectors.toList()));
        }
        
        // Legacy fields - set from first time schedule if available
        if (entity.getTimeSchedules() != null && !entity.getTimeSchedules().isEmpty()) {
            response.setStartTime(entity.getTimeSchedules().get(0).getStartTime());
            response.setEndTime(entity.getTimeSchedules().get(0).getEndTime());
        }
        
        return response;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public List<String> getVideoUrls() { return videoUrls; }
    public void setVideoUrls(List<String> videoUrls) { this.videoUrls = videoUrls; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isImmediate() { return immediate; }
    public void setImmediate(boolean immediate) { this.immediate = immediate; }

    public Set<String> getTargetTVs() { return targetTVs; }
    public void setTargetTVs(Set<String> targetTVs) { this.targetTVs = targetTVs; }

    public List<TimeScheduleResponse> getTimeSchedules() { return timeSchedules; }
    public void setTimeSchedules(List<TimeScheduleResponse> timeSchedules) { this.timeSchedules = timeSchedules; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}
