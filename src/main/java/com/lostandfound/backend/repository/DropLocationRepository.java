package com.lostandfound.backend.repository;

import com.lostandfound.backend.models.DropOrClaimed;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DropLocationRepository extends JpaRepository<DropOrClaimed, String> {
    boolean existsByNameIgnoreCase(String name);
}