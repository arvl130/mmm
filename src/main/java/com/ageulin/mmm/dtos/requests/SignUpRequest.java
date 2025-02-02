package com.ageulin.mmm.dtos.requests;

import jakarta.validation.constraints.NotNull;

public record SignUpRequest(
    @NotNull
    String name,
    @NotNull
    String email,
    @NotNull
    String password
) {
}
