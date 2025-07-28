package org.hyper.notificationbackend.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

import org.hyper.notificationbackend.models.ContentSchedule.ContentType;

@Entity
@Table(name = "profile_slides")
public class ProfileSlide {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    @JsonBackReference
    private TVProfile profile;
    
    @Column(nullable = false)
    private Integer slideOrder; // 1, 2, or 3 (max 3 slides per profile)
    
    @Column(nullable = false, length = 255)
    private String title;
    
    @Column(length = 1000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", length = 50, nullable = false)
    private ContentType contentType;
    
    // For storing text content or embed HTML
    @Column(columnDefinition = "LONGTEXT")
    private String content;
    
    // For storing multiple image URLs
    @ElementCollection
    @CollectionTable(name = "profile_slide_images", joinColumns = @JoinColumn(name = "slide_id"))
    @Column(name = "image_url", columnDefinition = "LONGTEXT")
    private List<String> imageUrls = new ArrayList<>();
    
    // For storing video URLs
    @ElementCollection
    @CollectionTable(name = "profile_slide_videos", joinColumns = @JoinColumn(name = "slide_id"))
    @Column(name = "video_url", columnDefinition = "LONGTEXT")
    private List<String> videoUrls = new ArrayList<>();
    
    // Duration in seconds (how long this slide should be displayed)
    @Column(nullable = false)
    private Integer durationSeconds = 10; // Default 10 seconds
    
    @Column(nullable = false)
    private boolean active = true;
    
    // Constructors
    public ProfileSlide() {}
    
    public ProfileSlide(TVProfile profile, Integer slideOrder, String title, ContentType contentType) {
        this.profile = profile;
        this.slideOrder = slideOrder;
        this.title = title;
        this.contentType = contentType;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public TVProfile getProfile() {
        return profile;
    }
    
    public void setProfile(TVProfile profile) {
        this.profile = profile;
    }
    
    public Integer getSlideOrder() {
        return slideOrder;
    }
    
    public void setSlideOrder(Integer slideOrder) {
        this.slideOrder = slideOrder;
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
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
    }
    
    public List<String> getVideoUrls() {
        return videoUrls;
    }
    
    public void setVideoUrls(List<String> videoUrls) {
        this.videoUrls = videoUrls != null ? videoUrls : new ArrayList<>();
    }
    
    public Integer getDurationSeconds() {
        return durationSeconds;
    }
    
    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds != null ? durationSeconds : 10;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    // Helper methods
    public void addImageUrl(String imageUrl) {
        if (imageUrls == null) {
            imageUrls = new ArrayList<>();
        }
        imageUrls.add(imageUrl);
    }
    
    public void addVideoUrl(String videoUrl) {
        if (videoUrls == null) {
            videoUrls = new ArrayList<>();
        }
        videoUrls.add(videoUrl);
    }
    
    public void clearImageUrls() {
        if (imageUrls != null) {
            imageUrls.clear();
        }
    }
    
    public void clearVideoUrls() {
        if (videoUrls != null) {
            videoUrls.clear();
        }
    }
}
