package org.hyper.notificationbackend.repositories;

import org.hyper.notificationbackend.models.TV;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TVRepository extends JpaRepository<TV, Long> {
    
    // Find all active TVs
    List<TV> findByActiveTrue();
    
    // Find TV by name (case insensitive)
    Optional<TV> findByNameIgnoreCase(String name);
    
    // Find TV by name and active status
    Optional<TV> findByNameIgnoreCaseAndActiveTrue(String name);
    
    // Find TVs by display name containing (case insensitive)
    @Query("SELECT t FROM TV t WHERE LOWER(t.displayName) LIKE LOWER(CONCAT('%', :displayName, '%')) AND t.active = true ORDER BY t.displayName ASC")
    List<TV> findByDisplayNameContainingIgnoreCase(@Param("displayName") String displayName);
    
    // Find TVs by location (case insensitive)
    @Query("SELECT t FROM TV t WHERE LOWER(t.location) LIKE LOWER(CONCAT('%', :location, '%')) AND t.active = true ORDER BY t.displayName ASC")
    List<TV> findByLocationContainingIgnoreCase(@Param("location") String location);
    
    // Check if TV name exists (for validation)
    boolean existsByNameIgnoreCase(String name);
    
    // Check if TV name exists excluding a specific ID (for updates)
    @Query("SELECT COUNT(t) > 0 FROM TV t WHERE LOWER(t.name) = LOWER(:name) AND t.id != :id")
    boolean existsByNameIgnoreCaseAndIdNot(@Param("name") String name, @Param("id") Long id);
    
    // Get all TVs ordered by name
    @Query("SELECT t FROM TV t ORDER BY t.name ASC")
    List<TV> findAllOrderByName();
    
    // Get all active TVs ordered by name
    @Query("SELECT t FROM TV t WHERE t.active = true ORDER BY t.name ASC")
    List<TV> findAllActiveOrderByName();
}
