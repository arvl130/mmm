package com.ageulin.mmm.dtos.requests;

import jakarta.validation.constraints.NotNull;

public record SendPasswordResetLinkRequest(
    @NotNull
    String email
) {
}
