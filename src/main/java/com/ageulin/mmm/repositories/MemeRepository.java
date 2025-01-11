package com.ageulin.mmm.repositories;

import com.ageulin.mmm.entities.Meme;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemeRepository extends CrudRepository<Meme, UUID> {
    List<Meme> findByUserId(UUID userId);
    Optional<Meme> findByIdAndUserId(UUID id, UUID userId);
    List<Meme> findDistinctByUserIdAndKeywords_NameContaining(UUID userId, String name);
    @Modifying
    @Query(
        value = """
        UPDATE memes SET searchable = to_tsvector('english', :searchable)
        WHERE id = :id AND user_id = :userId
        """,
        nativeQuery = true
    )
    void updateSearchableByIdAndUserId(UUID id, UUID userId, String searchable);

    @Modifying
    @Query(
        value = """
        SELECT
            m.*,
            ts_rank(searchable, websearch_to_tsquery('english', :searchTerm)) AS rank
        FROM memes m
        WHERE m.searchable @@ websearch_to_tsquery('english', :searchTerm)
        ORDER BY rank DESC
        """,
        nativeQuery = true
    )
    List<Meme> websearch(String searchTerm);
}
