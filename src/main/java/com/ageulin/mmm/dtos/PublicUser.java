package com.ageulin.mmm.dtos;

import java.util.UUID;

public record PublicUser(
    UUID id,
    String email,
    String avatarUrl
) {
}
