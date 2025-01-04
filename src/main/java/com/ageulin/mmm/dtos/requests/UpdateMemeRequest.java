package com.ageulin.mmm.dtos.requests;

import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record UpdateMemeRequest(
    @NotNull
    Set<String> keywords
) {
}
