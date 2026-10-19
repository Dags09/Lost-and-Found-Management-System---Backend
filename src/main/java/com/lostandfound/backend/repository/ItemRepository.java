package com.lostandfound.backend.repository;

import com.lostandfound.backend.models.Items;
import com.lostandfound.backend.models.enumRoleStatusTypes.ItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<Items, String> {
    List<Items> findByStatusOrderByCreatedAtDesc(ItemStatus status);
    List<Items> findByReporterIdOrderByCreatedAtDesc(String reporterId);
    boolean existsByCategoryId(String categoryId);
    boolean existsByDropLocationId(String dropLocationId);

    /** Public listing without a search phrase: open items, newest first. */
    @Query(value = """
            SELECT * FROM items i
            WHERE i.status = 'OPEN'
              AND (CAST(:categoryId AS varchar) IS NULL OR i.category_id = CAST(:categoryId AS varchar))
              AND (CAST(:dropLocationId AS varchar) IS NULL OR i.drop_location_id = CAST(:dropLocationId AS varchar))
            ORDER BY i.created_at DESC
            """,
            countQuery = """
            SELECT count(*) FROM items i
            WHERE i.status = 'OPEN'
              AND (CAST(:categoryId AS varchar) IS NULL OR i.category_id = CAST(:categoryId AS varchar))
              AND (CAST(:dropLocationId AS varchar) IS NULL OR i.drop_location_id = CAST(:dropLocationId AS varchar))
            """,
            nativeQuery = true)
    Page<Items> browseOpen(@Param("categoryId") String categoryId,
                           @Param("dropLocationId") String dropLocationId,
                           Pageable pageable);

    /**
     * Public listing with a search phrase: full-text match on name/description
     * (Postgres tsvector + GIN index), best match first. {@code tsQuery} is a
     * to_tsquery expression such as "black | wallet", built by ItemService.
     */
    @Query(value = """
            SELECT * FROM items i
            WHERE i.status = 'OPEN'
              AND i.search_vector @@ to_tsquery('english', :tsQuery)
              AND (CAST(:categoryId AS varchar) IS NULL OR i.category_id = CAST(:categoryId AS varchar))
              AND (CAST(:dropLocationId AS varchar) IS NULL OR i.drop_location_id = CAST(:dropLocationId AS varchar))
            ORDER BY ts_rank(i.search_vector, to_tsquery('english', :tsQuery)) DESC, i.created_at DESC
            """,
            countQuery = """
            SELECT count(*) FROM items i
            WHERE i.status = 'OPEN'
              AND i.search_vector @@ to_tsquery('english', :tsQuery)
              AND (CAST(:categoryId AS varchar) IS NULL OR i.category_id = CAST(:categoryId AS varchar))
              AND (CAST(:dropLocationId AS varchar) IS NULL OR i.drop_location_id = CAST(:dropLocationId AS varchar))
            """,
            nativeQuery = true)
    Page<Items> searchOpen(@Param("tsQuery") String tsQuery,
                           @Param("categoryId") String categoryId,
                           @Param("dropLocationId") String dropLocationId,
                           Pageable pageable);
}