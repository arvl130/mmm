package com.ageulin.mmm.repositories;

import com.ageulin.mmm.entities.Keyword;
import org.springframework.data.repository.CrudRepository;

import java.math.BigInteger;
import java.util.Optional;

public interface KeywordRepository extends CrudRepository<Keyword, BigInteger> {
    public Optional<Keyword>findByName(String name);
}
