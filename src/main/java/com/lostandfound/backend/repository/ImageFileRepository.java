package com.lostandfound.backend.repository;

import com.lostandfound.backend.models.ImageFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface ImageFileRepository extends JpaRepository<ImageFile, String> {

    // Bulk delete by id: does not load the (large) bytes first.
    @Modifying
    @Query("delete from ImageFile i where i.id in :ids")
    int deleteByIds(@Param("ids") Collection<String> ids);
}