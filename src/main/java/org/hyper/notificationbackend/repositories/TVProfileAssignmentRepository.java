package org.hyper.notificationbackend.repositories;

import org.hyper.notificationbackend.models.TVProfileAssignment;
import org.hyper.notificationbackend.models.TVEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TVProfileAssignmentRepository extends JpaRepository<TVProfileAssignment, Long> {
    
    // Find current assignment for a TV
    Optional<TVProfileAssignment> findByTvNameAndActiveTrue(TVEnum tvName);
    
    // Find all active assignments
    List<TVProfileAssignment> findByActiveTrue();
    
    // Find assignments for a specific profile
    List<TVProfileAssignment> findByProfileIdAndActiveTrue(Long profileId);
    
    // Find assignment with profile details fetched
    @Query("SELECT a FROM TVProfileAssignment a LEFT JOIN FETCH a.profile p LEFT JOIN FETCH p.slides WHERE a.tvName = :tvName AND a.active = true")
    Optional<TVProfileAssignment> findByTvNameWithProfile(@Param("tvName") TVEnum tvName);
    
    // Deactivate all assignments for a TV (used when assigning a new profile)
    @Modifying
    @Query("UPDATE TVProfileAssignment a SET a.active = false WHERE a.tvName = :tvName AND a.active = true")
    void deactivateAssignmentsForTV(@Param("tvName") TVEnum tvName);
}
