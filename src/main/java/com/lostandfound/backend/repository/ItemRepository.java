package com.lostandfound.backend.repository;

import com.lostandfound.backend.models.Items;
import com.lostandfound.backend.models.enumRoleStatusTypes.ItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<Items, String> {
    List<Items> findByStatusOrderByCreatedAtDesc(ItemStatus status);
    List<Items> findByReporterIdOrderByCreatedAtDesc(String reporterId);
    boolean existsByCategoryId(String categoryId);
    boolean existsByDropLocationId(String dropLocationId);

    /**
     * Flips an item OPEN -> CLAIMED in one database step. Returns 1 if it did, 0 if the item
     * was no longer OPEN (so two staff approving at the same moment cannot both win).
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Items i set i.status = com.lostandfound.backend.models.enumRoleStatusTypes.ItemStatus.CLAIMED, "
            + "i.updatedAt = :now where i.id = :id and i.status = com.lostandfound.backend.models.enumRoleStatusTypes.ItemStatus.OPEN")
    int markClaimedIfOpen(@Param("id") String id, @Param("now") java.time.Instant now);

    /**
     * Ids of FOUND items that are still OPEN, older than {@code cutoff}, and have no PENDING claim
     * (an item someone is actively claiming is never expired). Oldest first; pass a page to cap the batch.
     */
    @Query("select i.id from Items i where i.type = com.lostandfound.backend.models.enumRoleStatusTypes.ItemType.FOUND "
            + "and i.status = com.lostandfound.backend.models.enumRoleStatusTypes.ItemStatus.OPEN "
            + "and i.createdAt < :cutoff "
            + "and not exists (select 1 from Claim c where c.itemId = i.id "
            + "and c.status = com.lostandfound.backend.models.enumRoleStatusTypes.ClaimStatus.PENDING) "
            + "order by i.createdAt asc")
    List<String> findExpirableIds(@Param("cutoff") java.time.Instant cutoff, Pageable pageable);

    /**
     * Flips one item OPEN -> EXPIRED in a single database step, re-checking every condition so a claim
     * that arrives between the lookup and the update wins. Returns 1 if it expired, 0 if not.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Items i set i.status = com.lostandfound.backend.models.enumRoleStatusTypes.ItemStatus.EXPIRED, "
            + "i.updatedAt = :now where i.id = :id "
            + "and i.type = com.lostandfound.backend.models.enumRoleStatusTypes.ItemType.FOUND "
            + "and i.status = com.lostandfound.backend.models.enumRoleStatusTypes.ItemStatus.OPEN "
            + "and i.createdAt < :cutoff "
            + "and not exists (select 1 from Claim c where c.itemId = i.id "
            + "and c.status = com.lostandfound.backend.models.enumRoleStatusTypes.ClaimStatus.PENDING)")
    int markExpiredIfStale(@Param("id") String id,
                           @Param("cutoff") java.time.Instant cutoff,
                           @Param("now") java.time.Instant now);

    /** Public listing without a search phrase: open items, newest first. */
    @Query(value = """
            SELECT * FROM items i
            WHERE i.status = 'OPEN'
              AND (CAST(:categoryId AS varchar) IS NULL OR i.category_id = CAST(:categoryId AS varchar))
            ORDER BY i.created_at DESC
            """,
            countQuery = """
            SELECT count(*) FROM items i
            WHERE i.status = 'OPEN'
              AND (CAST(:categoryId AS varchar) IS NULL OR i.category_id = CAST(:categoryId AS varchar))
            """,
            nativeQuery = true)
    Page<Items> browseOpen(@Param("categoryId") String categoryId,
                           Pageable pageable);

    /**
     * Public listing with a search phrase: full-text match on name/description
     * (Postgres tsvector + GIN index), best match first. {@code tsQuery} is a
     * to_tsquery expression such as "black | wallet", built by ItemService.
     */
    @Query(value = """
        SELECT * FROM items i
        WHERE i.status = 'OPEN'
          AND (i.search_vector @@ to_tsquery('english', :tsQuery)
               OR word_similarity(CAST(:phrase AS text), i.name) > 0.4)
          AND (CAST(:categoryId AS varchar) IS NULL OR i.category_id = CAST(:categoryId AS varchar))
        ORDER BY (ts_rank(i.search_vector, to_tsquery('english', :tsQuery))
                  + word_similarity(CAST(:phrase AS text), i.name)) DESC,
                 i.created_at DESC
        """,
            countQuery = """
        SELECT count(*) FROM items i
        WHERE i.status = 'OPEN'
          AND (i.search_vector @@ to_tsquery('english', :tsQuery)
               OR word_similarity(CAST(:phrase AS text), i.name) > 0.4)
          AND (CAST(:categoryId AS varchar) IS NULL OR i.category_id = CAST(:categoryId AS varchar))
        """,
            nativeQuery = true)
    Page<Items> searchOpen(@Param("tsQuery") String tsQuery,
                           @Param("phrase") String phrase,
                           @Param("categoryId") String categoryId,
                           Pageable pageable);
}