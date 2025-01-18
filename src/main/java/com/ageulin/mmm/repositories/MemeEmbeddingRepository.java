package com.ageulin.mmm.repositories;

import com.ageulin.mmm.entities.MemeEmbedding;
import org.springframework.data.repository.CrudRepository;

import java.math.BigInteger;

public interface MemeEmbeddingRepository extends CrudRepository<MemeEmbedding, BigInteger> {
}
