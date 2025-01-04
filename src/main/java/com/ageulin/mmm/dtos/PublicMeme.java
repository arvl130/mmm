package com.ageulin.mmm.dtos;

import java.util.UUID;

public record PublicMeme(
    UUID id,
    String imgUrl
) {
}
