package com.ageulin.mmm.repositories;

import com.ageulin.mmm.entities.Meme;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemeRepository extends CrudRepository<Meme, UUID> {
    List<Meme> findByUserId(UUID userId);
    Optional<Meme> findByIdAndUserId(UUID id, UUID userId);
}
