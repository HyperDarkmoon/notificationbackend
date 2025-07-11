package org.hyper.notificationbackend.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "content_schedules")
public class ContentSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", length = 50)
    private ContentType contentType;
    
    // For storing image URLs or embed HTML
    @Column(columnDefinition = "LONGTEXT")
    private String content;
    
    // For storing multiple image URLs (up to 4)
    @ElementCollection
    @CollectionTable(name = "content_images", joinColumns = @JoinColumn(name = "schedule_id"))
    @Column(name = "image_url", columnDefinition = "LONGTEXT")
    private List<String> imageUrls = new ArrayList<>();
    
    // For storing video URLs
    @ElementCollection
    @CollectionTable(name = "content_videos", joinColumns = @JoinColumn(name = "schedule_id"))
    @Column(name = "video_url", columnDefinition = "LONGTEXT")
    private List<String> videoUrls = new ArrayList<>();
    
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean active = true;
    
    // Specify which TVs should display this content using the enum
    @ElementCollection
    @CollectionTable(name = "content_tv_mapping", joinColumns = @JoinColumn(name = "schedule_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "tv_enum")
    private Set<TVEnum> targetTVs = EnumSet.noneOf(TVEnum.class);
    
    public enum ContentType {
        IMAGE_SINGLE,   // Single image
        IMAGE_DUAL,     // Two images
        IMAGE_QUAD,     // Four images
        VIDEO,          // Video content
        EMBED,          // Embedded content (iframe, video, etc.)
        TEXT            // Text content
    }
    
    // Constructors
    public ContentSchedule() {}
    
    public ContentSchedule(String title, ContentType contentType, LocalDateTime startTime, LocalDateTime endTime) {
        this.title = title;
        this.contentType = contentType;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public ContentType getContentType() {
        return contentType;
    }
    
    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public List<String> getImageUrls() {
        return imageUrls;
    }
    
    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
    
    public void addImageUrl(String imageUrl) {
        this.imageUrls.add(imageUrl);
    }
    
    public List<String> getVideoUrls() {
        return videoUrls;
    }
    
    public void setVideoUrls(List<String> videoUrls) {
        this.videoUrls = videoUrls;
    }
    
    public void addVideoUrl(String videoUrl) {
        this.videoUrls.add(videoUrl);
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
    
    public Set<TVEnum> getTargetTVs() {
        return targetTVs;
    }
    
    public void setTargetTVs(Set<TVEnum> targetTVs) {
        this.targetTVs = targetTVs;
    }
    
    public void addTargetTV(TVEnum tv) {
        this.targetTVs.add(tv);
    }
    
    public void removeTargetTV(TVEnum tv) {
        this.targetTVs.remove(tv);
    }
}
