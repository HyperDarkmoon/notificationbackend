package org.hyper.notificationbackend.repositories;

import org.hyper.notificationbackend.models.TV;
import org.hyper.notificationbackend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TVRepository extends JpaRepository<TV, Long> {
    List<TV> findByOwner(User owner);
    List<TV> findByIsActive(Boolean isActive);
    List<TV> findByOwnerAndIsActive(User owner, Boolean isActive);
}
