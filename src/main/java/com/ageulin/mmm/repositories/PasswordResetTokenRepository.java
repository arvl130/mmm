package com.ageulin.mmm.repositories;

import com.ageulin.mmm.entities.PasswordResetToken;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface PasswordResetTokenRepository extends CrudRepository<PasswordResetToken, UUID> {
}
