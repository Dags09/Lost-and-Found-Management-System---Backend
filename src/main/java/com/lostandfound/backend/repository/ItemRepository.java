package com.lostandfound.backend.repository;

import com.lostandfound.backend.models.Items;
import com.lostandfound.backend.models.enumRoleStatusTypes.ItemStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ItemRepository extends MongoRepository<Items, String> {
    List<Items> findByStatusOrderByCreatedAtDesc(ItemStatus status);
}