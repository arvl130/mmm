package com.ageulin.mmm.dtos;

import java.util.List;
import java.util.UUID;

public record PublicMeme(
    UUID id,
    String imgUrl,
    List<PublicKeyword> keywords
) {
}
