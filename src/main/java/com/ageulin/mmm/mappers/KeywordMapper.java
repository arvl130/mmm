package com.ageulin.mmm.mappers;

import com.ageulin.mmm.dtos.PublicKeyword;
import com.ageulin.mmm.entities.Keyword;

public class KeywordMapper {
    public static PublicKeyword toPublic(Keyword keyword) {
        return new PublicKeyword(keyword.getId(), keyword.getName());
    }
}
