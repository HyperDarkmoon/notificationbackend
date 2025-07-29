package org.hyper.notificationbackend.repositories;

import org.hyper.notificationbackend.models.ProfileTimeSchedule;
import org.hyper.notificationbackend.models.TVProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProfileTimeScheduleRepository extends JpaRepository<ProfileTimeSchedule, Long> {
    
    // Find all time schedules for a specific profile
    List<ProfileTimeSchedule> findByTvProfileAndActiveTrue(TVProfile tvProfile);
    
    // Find all time schedules for a profile ID
    List<ProfileTimeSchedule> findByTvProfileIdAndActiveTrue(Long profileId);
    
    // Find currently active time schedules for a profile
    @Query("SELECT pts FROM ProfileTimeSchedule pts WHERE pts.tvProfile = :profile AND pts.active = true AND pts.startTime <= :currentTime AND pts.endTime >= :currentTime")
    List<ProfileTimeSchedule> findCurrentlyActiveSchedules(@Param("profile") TVProfile profile, @Param("currentTime") LocalDateTime currentTime);
    
    // Find upcoming time schedules for a profile
    @Query("SELECT pts FROM ProfileTimeSchedule pts WHERE pts.tvProfile = :profile AND pts.active = true AND pts.startTime > :currentTime ORDER BY pts.startTime ASC")
    List<ProfileTimeSchedule> findUpcomingSchedules(@Param("profile") TVProfile profile, @Param("currentTime") LocalDateTime currentTime);
    
    // Find expired time schedules for a profile
    @Query("SELECT pts FROM ProfileTimeSchedule pts WHERE pts.tvProfile = :profile AND pts.endTime < :currentTime ORDER BY pts.endTime DESC")
    List<ProfileTimeSchedule> findExpiredSchedules(@Param("profile") TVProfile profile, @Param("currentTime") LocalDateTime currentTime);
    
    // Check if there are any currently active schedules for a profile
    @Query("SELECT COUNT(pts) > 0 FROM ProfileTimeSchedule pts WHERE pts.tvProfile = :profile AND pts.active = true AND pts.startTime <= :currentTime AND pts.endTime >= :currentTime")
    boolean hasActiveSchedules(@Param("profile") TVProfile profile, @Param("currentTime") LocalDateTime currentTime);
    
    // Find overlapping time schedules for a profile (useful for validation)
    @Query("SELECT pts FROM ProfileTimeSchedule pts WHERE pts.tvProfile = :profile AND pts.active = true AND " +
           "((pts.startTime <= :startTime AND pts.endTime > :startTime) OR " +
           "(pts.startTime < :endTime AND pts.endTime >= :endTime) OR " +
           "(pts.startTime >= :startTime AND pts.endTime <= :endTime))")
    List<ProfileTimeSchedule> findOverlappingSchedules(@Param("profile") TVProfile profile, 
                                                      @Param("startTime") LocalDateTime startTime, 
                                                      @Param("endTime") LocalDateTime endTime);
    
    // Delete all schedules for a profile
    void deleteByTvProfile(TVProfile profile);
    
    // Delete all schedules for a profile ID
    void deleteByTvProfileId(Long profileId);
}
