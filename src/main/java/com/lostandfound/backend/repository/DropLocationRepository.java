package com.lostandfound.backend.repository;

import com.lostandfound.backend.models.DropOrClaimed;
import org.springframework.data.jpa.repository.JpaRepository;

/** Database access for drop-off locations. */
public interface DropLocationRepository extends JpaRepository<DropOrClaimed, String> {
    /** True if a location with this name exists, ignoring upper/lower case. */
    boolean existsByNameIgnoreCase(String name);
}