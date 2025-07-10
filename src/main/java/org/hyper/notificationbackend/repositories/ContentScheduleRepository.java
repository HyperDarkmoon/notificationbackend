package org.hyper.notificationbackend.repositories;

import org.hyper.notificationbackend.models.ContentSchedule;
import org.hyper.notificationbackend.models.TVEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContentScheduleRepository extends JpaRepository<ContentSchedule, Long> {
    // Find schedules that are currently active (current time is between start and end time)
    @Query("SELECT c FROM ContentSchedule c WHERE c.active = true AND c.startTime <= ?1 AND c.endTime >= ?1")
    List<ContentSchedule> findCurrentlyActive(LocalDateTime currentTime);
    
    // Find upcoming schedules (start time is in the future)
    @Query("SELECT c FROM ContentSchedule c WHERE c.active = true AND c.startTime > ?1 ORDER BY c.startTime ASC")
    List<ContentSchedule> findUpcoming(LocalDateTime currentTime);
    
    // Find schedules for a specific TV
    @Query("SELECT c FROM ContentSchedule c JOIN c.targetTVs t WHERE t = ?1 AND c.active = true")
    List<ContentSchedule> findByTV(TVEnum tv);
    
    // Find upcoming schedules for a specific TV
    @Query("SELECT c FROM ContentSchedule c JOIN c.targetTVs t WHERE t = ?1 AND c.active = true AND c.startTime > ?2 ORDER BY c.startTime ASC")
    List<ContentSchedule> findUpcomingForTV(TVEnum tv, LocalDateTime currentTime);
}
