package com.ageulin.mmm.repositories;

import com.ageulin.mmm.entities.Meme;
import com.ageulin.mmm.entities.MemeEmbedding;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.math.BigInteger;
import java.util.UUID;

public interface MemeEmbeddingRepository extends CrudRepository<MemeEmbedding, BigInteger> {
    // This is dumb. Why do we need a custom query for this?
    @Modifying
    @Query(
        value = """
            DELETE FROM meme_embeddings me
            WHERE me.meme_id = :memeId
        """,
        nativeQuery = true
    )
    void deleteByMemeId(UUID memeId);
}
