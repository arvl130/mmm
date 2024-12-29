package com.ageulin.mmm.dtos.responses;

import com.ageulin.mmm.dtos.PublicUser;

public record CurrentUserResponse(
    String message,
    PublicUser result
) {
}
