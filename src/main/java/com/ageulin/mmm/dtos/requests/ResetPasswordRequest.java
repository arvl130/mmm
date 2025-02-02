package com.ageulin.mmm.dtos.requests;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ResetPasswordRequest(
    @NotNull
    UUID token,
    @NotNull
    String newPassword
) {
}
