package com.ageulin.mmm.dtos.requests;

import jakarta.validation.constraints.NotNull;

import java.util.Set;
import java.util.UUID;

public record StoreMemeRequest(
    @NotNull
    UUID id,
    @NotNull
    Set<String> keywords
) {
}
