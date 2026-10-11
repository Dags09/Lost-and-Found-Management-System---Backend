package com.lostandfound.backend.repository;

import com.lostandfound.backend.models.DropOrClaimed;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DropLocationRepository extends MongoRepository<DropOrClaimed, String> {
    boolean existsByNameIgnoreCase(String name);
}