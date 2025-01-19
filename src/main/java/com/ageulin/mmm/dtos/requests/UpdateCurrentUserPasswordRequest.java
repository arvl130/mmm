package com.ageulin.mmm.dtos.requests;

public record UpdateCurrentUserPasswordRequest(
    String oldPassword,
    String newPassword
) {
}
