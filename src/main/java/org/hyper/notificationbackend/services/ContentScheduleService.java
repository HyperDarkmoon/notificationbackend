package org.hyper.notificationbackend.services;

import org.hyper.notificationbackend.models.ContentSchedule;
import org.hyper.notificationbackend.models.TimeSchedule;
import org.hyper.notificationbackend.models.TVEnum;
import org.hyper.notificationbackend.repositories.ContentScheduleRepository;
import org.hyper.notificationbackend.repositories.TimeScheduleRepository;
import org.hyper.notificationbackend.dto.ContentScheduleRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ContentScheduleService {
    
    @Autowired
    private ContentScheduleRepository contentScheduleRepository;
    
    @Autowired
    private TimeScheduleRepository timeScheduleRepository;
    
    // Convert DTO to entity with multiple time schedules support
        public ContentSchedule convertFromRequest(ContentScheduleRequest request) {
        ContentSchedule contentSchedule = new ContentSchedule();
        
        // Debug logging
        System.out.println("=== DEBUG: Converting ContentScheduleRequest ===");
        System.out.println("Request isImmediate: " + request.isImmediate());
        System.out.println("Request isDailySchedule: " + request.isDailySchedule());
        System.out.println("Request dailyStartTime: " + request.getDailyStartTime());
        System.out.println("Request dailyEndTime: " + request.getDailyEndTime());
        System.out.println("Request timeSchedules: " + (request.getTimeSchedules() != null ? request.getTimeSchedules().size() : "null"));
        System.out.println("Request startTime: " + request.getStartTime());
        System.out.println("Request endTime: " + request.getEndTime());
        
        contentSchedule.setTitle(request.getTitle());
        contentSchedule.setDescription(request.getDescription());
        contentSchedule.setContent(request.getContent());
        contentSchedule.setImageUrls(request.getImageUrls());
        contentSchedule.setVideoUrls(request.getVideoUrls());
        
        // Convert content type string to enum
        if (request.getContentType() != null) {
            contentSchedule.setContentType(ContentSchedule.ContentType.valueOf(request.getContentType()));
        }
        
        // Convert target TVs
        if (request.getTargetTVs() != null) {
            Set<TVEnum> targetTVs = request.getTargetTVs().stream()
                .map(TVEnum::valueOf)
                .collect(Collectors.toSet());
            contentSchedule.setTargetTVs(targetTVs);
        }
        
        // Handle time schedules - support daily schedule, multiple schedules, and legacy format
        List<TimeSchedule> timeSchedules = new ArrayList<>();
        
        System.out.println("=== DEBUG: Processing schedule type ===");
        
        // Check if this is explicitly set as immediate content
        if (request.isImmediate()) {
            System.out.println("DEBUG: Setting as IMMEDIATE content");
            contentSchedule.setImmediate(true);
            contentSchedule.setDailySchedule(false);
            System.out.println("DEBUG: After immediate - immediate is: " + contentSchedule.isImmediate());
        }
        // First, check if this is a daily schedule
        else if (request.isDailySchedule()) {
            System.out.println("DEBUG: Setting as DAILY SCHEDULE");
            contentSchedule.setDailySchedule(true);
            contentSchedule.setDailyStartTime(request.getDailyStartTime());
            contentSchedule.setDailyEndTime(request.getDailyEndTime());
            // Set timeSchedules FIRST before setting immediate flag
            // This is important because setTimeSchedules() automatically sets immediate=true for empty lists
            contentSchedule.setTimeSchedules(timeSchedules);
            // Now set immediate to false for daily schedules
            contentSchedule.setImmediate(false);
            System.out.println("DEBUG: Daily schedule - immediate set to: " + contentSchedule.isImmediate());
            // Skip the rest of the logic for daily schedules
        }
        // Check if we have new format time schedules (multiple schedules)
        else if (request.getTimeSchedules() != null && !request.getTimeSchedules().isEmpty()) {
            System.out.println("DEBUG: Processing time schedules");
            for (ContentScheduleRequest.TimeScheduleRequest tsRequest : request.getTimeSchedules()) {
                TimeSchedule timeSchedule = new TimeSchedule();
                timeSchedule.setStartTime(tsRequest.getStartTime());
                timeSchedule.setEndTime(tsRequest.getEndTime());
                // Set the relationship - this is crucial!
                timeSchedule.setContentSchedule(contentSchedule);
                timeSchedules.add(timeSchedule);
            }
            contentSchedule.setTimeSchedules(timeSchedules);
            contentSchedule.setImmediate(false);
            System.out.println("DEBUG: After time schedules - immediate is: " + contentSchedule.isImmediate());
        } 
        // Check legacy format (single start/end time)
        else if (request.getStartTime() != null && request.getEndTime() != null) {
            System.out.println("DEBUG: Processing legacy format");
            TimeSchedule timeSchedule = new TimeSchedule();
            timeSchedule.setStartTime(request.getStartTime());
            timeSchedule.setEndTime(request.getEndTime());
            // Set the relationship - this is crucial!
            timeSchedule.setContentSchedule(contentSchedule);
            timeSchedules.add(timeSchedule);
            contentSchedule.setTimeSchedules(timeSchedules);
            contentSchedule.setImmediate(false);
            System.out.println("DEBUG: After legacy format - immediate is: " + contentSchedule.isImmediate());
        }
        // No time schedules and not daily schedule - immediate content
        else if (!request.isDailySchedule()) {
            System.out.println("DEBUG: No schedules found and not daily, setting as IMMEDIATE content");
            contentSchedule.setTimeSchedules(timeSchedules);
            contentSchedule.setImmediate(true);
            System.out.println("DEBUG: After setting immediate - immediate is: " + contentSchedule.isImmediate());
        }
        
        System.out.println("DEBUG: Before final debug - immediate is: " + contentSchedule.isImmediate());
        
        // Set active to true by default (since DTO doesn't include this field)
        contentSchedule.setActive(request.isActive());
        
        System.out.println("=== DEBUG: Final ContentSchedule state ===");
        System.out.println("Final isImmediate: " + contentSchedule.isImmediate());
        System.out.println("Final isDailySchedule: " + contentSchedule.isDailySchedule());
        System.out.println("Final dailyStartTime: " + contentSchedule.getDailyStartTime());
        System.out.println("Final dailyEndTime: " + contentSchedule.getDailyEndTime());
        System.out.println("=== END DEBUG ===");
        
        return contentSchedule;
    }
    
    // Create a new content schedule with multiple time schedules support
    @Transactional
    public ContentSchedule createSchedule(ContentSchedule contentSchedule) {
        System.out.println("DEBUG: createSchedule() - incoming immediate: " + contentSchedule.isImmediate());
        System.out.println("DEBUG: createSchedule() - incoming isDailySchedule: " + contentSchedule.isDailySchedule());
        
        validateSchedule(contentSchedule);
        
        boolean isImmediate = false;
        
        // Determine if this is immediate content - don't override if it's a daily schedule
        if (!contentSchedule.isDailySchedule()) {
            isImmediate = (contentSchedule.getTimeSchedules() == null || contentSchedule.getTimeSchedules().isEmpty());
            contentSchedule.setImmediate(isImmediate);
            System.out.println("DEBUG: Not a daily schedule, setting immediate to: " + isImmediate);
        } else {
            isImmediate = contentSchedule.isImmediate();
            System.out.println("DEBUG: Is daily schedule, keeping immediate as: " + contentSchedule.isImmediate());
        }
        
        // Handle content override logic (skip for daily schedules - they are managed by the scheduled task)
        if (!contentSchedule.isDailySchedule()) {
            System.out.println("DEBUG: Calling handleContentOverride for non-daily schedule");
            handleContentOverride(contentSchedule);
        } else {
            System.out.println("DEBUG: SKIPPING handleContentOverride for daily schedule - will be managed by scheduled task");
        }
        

        // FIXED: Properly set up bidirectional relationship before saving
        if (!isImmediate && contentSchedule.getTimeSchedules() != null) {
            for (TimeSchedule timeSchedule : contentSchedule.getTimeSchedules()) {
                timeSchedule.setContentSchedule(contentSchedule);
            }
        }
        
        // Save the content schedule with cascaded time schedules
        // The cascade should handle saving the time schedules automatically
        System.out.println("DEBUG: About to save ContentSchedule to database");
        ContentSchedule savedSchedule = contentScheduleRepository.save(contentSchedule);
        System.out.println("DEBUG: ContentSchedule saved successfully with ID: " + savedSchedule.getId());
        
        return savedSchedule;
    }
    
    // Create schedule from request DTO (new method)
    @Transactional
    public ContentSchedule createScheduleFromRequest(ContentScheduleRequest request) {
        ContentSchedule contentSchedule = convertFromRequest(request);
        return createSchedule(contentSchedule);
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
        LocalDateTime now = LocalDateTime.now();
        List<ContentSchedule> activeSchedules = new ArrayList<>();
        
        // Get immediate schedules
        List<ContentSchedule> immediateSchedules = contentScheduleRepository.findImmediateSchedules();
        activeSchedules.addAll(immediateSchedules);
        
        // Get schedules with active time schedules
        List<TimeSchedule> activeTimeSchedules = timeScheduleRepository.findCurrentlyActive(now);
        for (TimeSchedule timeSchedule : activeTimeSchedules) {
            if (!activeSchedules.contains(timeSchedule.getContentSchedule())) {
                activeSchedules.add(timeSchedule.getContentSchedule());
            }
        }
        
        return activeSchedules;
    }
    
    // Get upcoming schedules
    public List<ContentSchedule> getUpcomingSchedules() {
        LocalDateTime now = LocalDateTime.now();
        List<ContentSchedule> upcomingSchedules = new ArrayList<>();
        
        List<TimeSchedule> upcomingTimeSchedules = timeScheduleRepository.findUpcoming(now);
        for (TimeSchedule timeSchedule : upcomingTimeSchedules) {
            if (!upcomingSchedules.contains(timeSchedule.getContentSchedule())) {
                upcomingSchedules.add(timeSchedule.getContentSchedule());
            }
        }
        
        return upcomingSchedules;
    }
    
    // Get immediate/indefinite schedules
    public List<ContentSchedule> getImmediateSchedules() {
        return contentScheduleRepository.findImmediateSchedules();
    }
    
    // Get schedules for a specific TV (prioritized by immediate vs scheduled)
    public List<ContentSchedule> getSchedulesForTV(TVEnum tv) {
        LocalDateTime now = LocalDateTime.now();
        List<ContentSchedule> result = new ArrayList<>();
        
        // First, get any scheduled content that's currently active (higher priority)
        List<TimeSchedule> activeTimeSchedules = timeScheduleRepository.findCurrentlyActiveForTV(tv, now);
        for (TimeSchedule timeSchedule : activeTimeSchedules) {
            ContentSchedule content = timeSchedule.getContentSchedule();
            if (content.isActive() && !result.contains(content)) {
                result.add(content);
            }
        }
        
        // Next, check for daily scheduled content that's currently active
        if (result.isEmpty()) {
            List<ContentSchedule> dailySchedules = contentScheduleRepository.findDailyScheduleForTV(tv);
            for (ContentSchedule content : dailySchedules) {
                if (content.isActive() && content.isDailyScheduleActive(now) && !result.contains(content)) {
                    result.add(content);
                }
            }
        }
        
        // If no scheduled content is active, get immediate content
        if (result.isEmpty()) {
            List<ContentSchedule> immediateSchedules = contentScheduleRepository.findImmediateForTV(tv);
            result.addAll(immediateSchedules.stream()
                .filter(ContentSchedule::isActive)
                .collect(Collectors.toList()));
        }
        
        return result;
    }
    
    // Get upcoming schedules for a specific TV
    public List<ContentSchedule> getUpcomingSchedulesForTV(TVEnum tv) {
        LocalDateTime now = LocalDateTime.now();
        List<ContentSchedule> upcomingSchedules = new ArrayList<>();
        
        List<TimeSchedule> upcomingTimeSchedules = timeScheduleRepository.findUpcomingForTV(tv, now);
        for (TimeSchedule timeSchedule : upcomingTimeSchedules) {
            if (!upcomingSchedules.contains(timeSchedule.getContentSchedule())) {
                upcomingSchedules.add(timeSchedule.getContentSchedule());
            }
        }
        
        return upcomingSchedules;
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
            existingSchedule.setActive(updatedSchedule.isActive());
            existingSchedule.setTargetTVs(updatedSchedule.getTargetTVs());
            
            // Handle time schedules updates
            if (updatedSchedule.getTimeSchedules() != null) {
                // Clear old time schedules properly
                existingSchedule.clearTimeSchedules();
                
                // Add new time schedules
                for (TimeSchedule timeSchedule : updatedSchedule.getTimeSchedules()) {
                    timeSchedule.setContentSchedule(existingSchedule);
                    existingSchedule.addTimeSchedule(timeSchedule);
                }
                existingSchedule.setImmediate(false);
            } else {
                // No time schedules provided - make it immediate content
                existingSchedule.clearTimeSchedules();
                existingSchedule.setImmediate(true);
            }
            
            return contentScheduleRepository.save(existingSchedule);
        }
        throw new RuntimeException("Content schedule not found with id: " + id);
    }
    
    // Delete a content schedule
    public void deleteSchedule(Long id) {
        contentScheduleRepository.deleteById(id);
    }
    
    // Handle content override logic for new schedules
    private void handleContentOverride(ContentSchedule newSchedule) {
        // For each target TV, handle existing content
        for (TVEnum tv : newSchedule.getTargetTVs()) {
            handleTVContentOverride(tv, newSchedule);
        }
    }
    
    // Handle content override for a specific TV
    private void handleTVContentOverride(TVEnum tv, ContentSchedule newSchedule) {
        System.out.println("DEBUG: handleTVContentOverride called for TV " + tv + " with schedule: " + newSchedule.getTitle());
        System.out.println("DEBUG: newSchedule.isImmediate(): " + newSchedule.isImmediate());
        System.out.println("DEBUG: newSchedule.isDailySchedule(): " + newSchedule.isDailySchedule());
        
        // Get all active immediate content for this TV
        List<ContentSchedule> existingImmediate = contentScheduleRepository.findImmediateForTV(tv);
        System.out.println("DEBUG: Found " + existingImmediate.size() + " existing immediate content for TV " + tv);
        
        for (ContentSchedule existing : existingImmediate) {
            System.out.println("DEBUG: Checking existing content: " + existing.getTitle() + " (active: " + existing.isActive() + ")");
            if (existing.isActive()) {
                if (newSchedule.isImmediate()) {
                    System.out.println("DEBUG: New immediate content overrides old immediate content - DISABLING " + existing.getTitle());
                    // New immediate content overrides old immediate content permanently
                    existing.setActive(false);
                    contentScheduleRepository.save(existing);
                } else if (!newSchedule.isDailySchedule()) {
                    System.out.println("DEBUG: New time-scheduled content will override when active - storing reference for " + existing.getTitle());
                    // New time-scheduled content will temporarily override immediate content when it becomes active
                    // Just store the reference for later use when the schedule actually starts
                    // DO NOT deactivate the existing content immediately
                    for (TimeSchedule timeSchedule : newSchedule.getTimeSchedules()) {
                        String existingDisabled = timeSchedule.getTemporarilyDisabledContentIds();
                        if (existingDisabled != null && !existingDisabled.isEmpty()) {
                            timeSchedule.setTemporarilyDisabledContentIds(existingDisabled + "," + existing.getId());
                        } else {
                            timeSchedule.setTemporarilyDisabledContentIds(existing.getId().toString());
                        }
                    }
                    // Note: We don't deactivate the existing content here anymore
                    // It will be deactivated by the scheduled task when the time schedule becomes active
                } else {
                    System.out.println("DEBUG: New schedule is DAILY SCHEDULE - NOT affecting existing content " + existing.getTitle());
                }
                // For daily schedules, we do NOTHING here - let the scheduled task handle it
                // Daily schedules will be managed by the scheduled task that checks if they're currently active
            }
        }
        
        // Handle overlapping scheduled content (only for time-based schedules, not daily schedules)
        if (!newSchedule.isImmediate() && !newSchedule.isDailySchedule()) {
            for (TimeSchedule newTimeSchedule : newSchedule.getTimeSchedules()) {
                // Find any existing time schedules that overlap with this new one using the repository method
                List<TimeSchedule> overlappingSchedules = timeScheduleRepository.findOverlappingForTV(
                    tv, newTimeSchedule.getStartTime(), newTimeSchedule.getEndTime());
                
                for (TimeSchedule overlapping : overlappingSchedules) {
                    // Skip if it's the same schedule (in case of updates)
                    if (overlapping.getContentSchedule().getId().equals(newSchedule.getId())) {
                        continue;
                    }
                    
                    // Disable the overlapping time schedule
                    overlapping.setActive(false);
                    timeScheduleRepository.save(overlapping);
                    
                    // Store reference to restore later if needed
                    String existingDisabled = newTimeSchedule.getTemporarilyDisabledContentIds();
                    if (existingDisabled != null && !existingDisabled.isEmpty()) {
                        newTimeSchedule.setTemporarilyDisabledContentIds(existingDisabled + "," + overlapping.getContentSchedule().getId());
                    } else {
                        newTimeSchedule.setTemporarilyDisabledContentIds(overlapping.getContentSchedule().getId().toString());
                    }
                }
            }
        }
    }
    
    // Method to handle content scheduling and restoration (called every minute)
    @Scheduled(fixedRate = 60000) // Run every minute
    public void manageScheduledContent() {
        LocalDateTime now = LocalDateTime.now();
        System.out.println("=== DEBUG: manageScheduledContent() running at: " + now + " ===");
        
        // 1. Handle starting schedules - deactivate content that should be temporarily disabled
        List<TimeSchedule> startingSchedules = timeScheduleRepository.findCurrentlyActive(now);
        System.out.println("DEBUG: Found " + startingSchedules.size() + " currently active time schedules");
        
        for (TimeSchedule startingSchedule : startingSchedules) {
            if (startingSchedule.isActive() && startingSchedule.getTemporarilyDisabledContentIds() != null && 
                !startingSchedule.getTemporarilyDisabledContentIds().isEmpty()) {
                
                String[] disabledIds = startingSchedule.getTemporarilyDisabledContentIds().split(",");
                for (String idStr : disabledIds) {
                    try {
                        Long disabledId = Long.parseLong(idStr.trim());
                        Optional<ContentSchedule> contentToDisableOpt = contentScheduleRepository.findById(disabledId);
                        if (contentToDisableOpt.isPresent()) {
                            ContentSchedule contentToDisable = contentToDisableOpt.get();
                            // Only disable if it's currently active and the schedule is currently running
                            if (contentToDisable.isActive() && startingSchedule.isCurrentlyActive(now)) {
                                System.out.println("DEBUG: Disabling content ID " + disabledId + " due to active time schedule");
                                contentToDisable.setActive(false);
                                contentScheduleRepository.save(contentToDisable);
                            }
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid content ID in temporarily disabled list: " + idStr);
                    }
                }
            }
        }
        
        // 1.5. Handle daily schedules - manage content override based on daily schedule windows
        List<ContentSchedule> allDailySchedules = contentScheduleRepository.findAll().stream()
            .filter(cs -> cs.isDailySchedule() && cs.isActive())
            .collect(Collectors.toList());
        
        System.out.println("DEBUG: Found " + allDailySchedules.size() + " active daily schedules");
        System.out.println("DEBUG: Current time for comparison: " + now.toLocalTime());
        
        // Track which TVs have active daily schedules
        Map<TVEnum, List<ContentSchedule>> activeDailySchedulesByTV = new HashMap<>();
        
        for (ContentSchedule dailySchedule : allDailySchedules) {
            boolean isCurrentlyInWindow = dailySchedule.isDailyScheduleActive(now);
            System.out.println("DEBUG: Daily schedule '" + dailySchedule.getTitle() + "' (ID: " + dailySchedule.getId() + ") - currently in window: " + isCurrentlyInWindow);
            System.out.println("DEBUG: Schedule window: " + dailySchedule.getDailyStartTime() + " - " + dailySchedule.getDailyEndTime());
            System.out.println("DEBUG: Target TVs: " + dailySchedule.getTargetTVs());
            
            if (isCurrentlyInWindow) {
                for (TVEnum tv : dailySchedule.getTargetTVs()) {
                    activeDailySchedulesByTV.computeIfAbsent(tv, k -> new ArrayList<>()).add(dailySchedule);
                    System.out.println("DEBUG: Added daily schedule '" + dailySchedule.getTitle() + "' to active list for TV " + tv);
                }
            }
        }
        
        // Get all TVs that have daily schedules configured (active or inactive)
        Set<TVEnum> allTVsWithDailySchedules = new HashSet<>();
        for (ContentSchedule dailySchedule : allDailySchedules) {
            allTVsWithDailySchedules.addAll(dailySchedule.getTargetTVs());
        }
        
        // For each TV, manage immediate content based on daily schedule status
        for (TVEnum tv : allTVsWithDailySchedules) {
            List<ContentSchedule> activeDailySchedulesForTV = activeDailySchedulesByTV.getOrDefault(tv, new ArrayList<>());
            boolean hasActiveDailySchedule = !activeDailySchedulesForTV.isEmpty();
            
            // FIXED: Look for ALL immediate content (both active and inactive) for proper restoration
            List<ContentSchedule> allImmediateForTV = contentScheduleRepository.findAll().stream()
                .filter(cs -> cs.isImmediate() && cs.getTargetTVs().contains(tv))
                .collect(Collectors.toList());
            
            System.out.println("DEBUG: TV " + tv + " - hasActiveDailySchedule: " + hasActiveDailySchedule + ", found " + allImmediateForTV.size() + " immediate content (active and inactive)");
            
            for (ContentSchedule existingContent : allImmediateForTV) {
                System.out.println("DEBUG: Checking immediate content '" + existingContent.getTitle() + "' (ID: " + existingContent.getId() + ") - isActive: " + existingContent.isActive());
                
                if (hasActiveDailySchedule && existingContent.isActive()) {
                    // Daily schedule is active - temporarily disable immediate content for this TV
                    System.out.println("DEBUG: DISABLING immediate content '" + existingContent.getTitle() + "' (ID: " + existingContent.getId() + ") for TV " + tv + " due to active daily schedule(s): " + 
                        activeDailySchedulesForTV.stream().map(ContentSchedule::getTitle).collect(Collectors.joining(", ")));
                    existingContent.setActive(false);
                    contentScheduleRepository.save(existingContent);
                } else if (!hasActiveDailySchedule && !existingContent.isActive()) {
                    // No daily schedules are active for this TV - restore immediate content
                    System.out.println("DEBUG: RESTORING immediate content '" + existingContent.getTitle() + "' (ID: " + existingContent.getId() + ") for TV " + tv + " - no active daily schedules");
                    existingContent.setActive(true);
                    contentScheduleRepository.save(existingContent);
                } else {
                    System.out.println("DEBUG: No action needed for immediate content '" + existingContent.getTitle() + "' (ID: " + existingContent.getId() + ") for TV " + tv + 
                        " - hasActiveDailySchedule: " + hasActiveDailySchedule + ", isActive: " + existingContent.isActive());
                }
            }
        }
        
        // 2. Handle expired schedules - restore temporarily disabled content
        List<TimeSchedule> expiredSchedules = timeScheduleRepository.findExpired(now);
        
        for (TimeSchedule expiredSchedule : expiredSchedules) {
            // Restore any content that was temporarily disabled by this schedule
            if (expiredSchedule.getTemporarilyDisabledContentIds() != null && 
                !expiredSchedule.getTemporarilyDisabledContentIds().isEmpty()) {
                
                String[] disabledIds = expiredSchedule.getTemporarilyDisabledContentIds().split(",");
                for (String idStr : disabledIds) {
                    try {
                        Long disabledId = Long.parseLong(idStr.trim());
                        Optional<ContentSchedule> disabledContentOpt = contentScheduleRepository.findById(disabledId);
                        if (disabledContentOpt.isPresent()) {
                            ContentSchedule disabledContent = disabledContentOpt.get();
                            
                            // Only restore if no other active schedule is currently disabling this content
                            boolean shouldRestore = true;
                            List<TimeSchedule> currentlyActiveSchedules = timeScheduleRepository.findCurrentlyActive(now);
                            for (TimeSchedule activeSchedule : currentlyActiveSchedules) {
                                if (activeSchedule.getTemporarilyDisabledContentIds() != null &&
                                    activeSchedule.getTemporarilyDisabledContentIds().contains(disabledId.toString())) {
                                    shouldRestore = false;
                                    break;
                                }
                            }
                            
                            if (shouldRestore) {
                                disabledContent.setActive(true);
                                contentScheduleRepository.save(disabledContent);
                            }
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid content ID in temporarily disabled list: " + idStr);
                    }
                }
            }
            
            // Deactivate the expired time schedule
            expiredSchedule.setActive(false);
            timeScheduleRepository.save(expiredSchedule);
        }
        
        // 3. Deactivate content schedules that only had timed content and all their time schedules are now expired
        List<ContentSchedule> allContentSchedules = contentScheduleRepository.findAll();
        for (ContentSchedule contentSchedule : allContentSchedules) {
            if (!contentSchedule.isImmediate() && !contentSchedule.isDailySchedule() && contentSchedule.isActive()) {
                // Check if all time schedules for this content are expired or inactive
                boolean hasActiveTimeSchedule = contentSchedule.getTimeSchedules().stream()
                    .anyMatch(ts -> ts.isActive() && !ts.isExpired(now));
                
                if (!hasActiveTimeSchedule) {
                    // No active time schedules, deactivate the content schedule
                    contentSchedule.setActive(false);
                    contentScheduleRepository.save(contentSchedule);
                }
            }
        }
    }
    
    // Validate schedule data
    private void validateSchedule(ContentSchedule schedule) {
        // Validate time schedules
        if (schedule.getTimeSchedules() != null && !schedule.getTimeSchedules().isEmpty()) {
            for (TimeSchedule timeSchedule : schedule.getTimeSchedules()) {
                if (timeSchedule.getStartTime() == null || timeSchedule.getEndTime() == null) {
                    throw new IllegalArgumentException("Both start time and end time must be provided for scheduled content");
                }
                if (timeSchedule.getStartTime().isAfter(timeSchedule.getEndTime())) {
                    throw new IllegalArgumentException("Start time must be before end time for scheduled content");
                }
                // Check that the schedule is not starting in the past (allow future scheduling)
                LocalDateTime now = LocalDateTime.now();
                if (timeSchedule.getStartTime().isBefore(now)) {
                    throw new IllegalArgumentException("Cannot schedule content to start in the past");
                }
            }
        }
        
        if (schedule.getContentType() == null) {
            throw new IllegalArgumentException("Content type must be provided");
        }
        
        // Validate content based on content type - allow multiple images for rotation
        if (schedule.getContentType() == ContentSchedule.ContentType.IMAGE_SINGLE && 
            (schedule.getImageUrls() == null || schedule.getImageUrls().size() < 1)) {
            throw new IllegalArgumentException("Single image content type requires at least one image URL");
        } else if (schedule.getContentType() == ContentSchedule.ContentType.IMAGE_DUAL && 
                  (schedule.getImageUrls() == null || schedule.getImageUrls().size() < 2)) {
            throw new IllegalArgumentException("Dual image content type requires at least two image URLs");
        } else if (schedule.getContentType() == ContentSchedule.ContentType.IMAGE_QUAD && 
                  (schedule.getImageUrls() == null || schedule.getImageUrls().size() < 4)) {
            throw new IllegalArgumentException("Quad image content type requires at least four image URLs");
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
