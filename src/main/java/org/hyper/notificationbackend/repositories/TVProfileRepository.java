package org.hyper.notificationbackend.repositories;

import org.hyper.notificationbackend.models.TVProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TVProfileRepository extends JpaRepository<TVProfile, Long> {
    
    // Find all active profiles
    List<TVProfile> findByActiveTrue();
    
    // Find profile by name
    Optional<TVProfile> findByNameAndActiveTrue(String name);
    
    // Find profiles with slides fetched  
    @Query("SELECT p FROM TVProfile p LEFT JOIN FETCH p.slides WHERE p.active = true ORDER BY p.name ASC")
    List<TVProfile> findAllActiveWithSlides();
    
    // Find profile by ID with slides fetched
    @Query("SELECT p FROM TVProfile p LEFT JOIN FETCH p.slides WHERE p.id = :id AND p.active = true")
    Optional<TVProfile> findByIdWithSlides(@Param("id") Long id);
    
    // Find profiles by name containing (case insensitive)
    @Query("SELECT p FROM TVProfile p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) AND p.active = true ORDER BY p.name ASC")
    List<TVProfile> findByNameContainingIgnoreCase(@Param("name") String name);
}
