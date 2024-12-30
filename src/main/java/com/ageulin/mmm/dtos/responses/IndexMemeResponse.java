package com.ageulin.mmm.dtos.responses;

import com.ageulin.mmm.entities.Meme;
import lombok.Getter;

import java.util.List;

@Getter
public class IndexMemeResponse extends BaseResponse {
    private final List<Meme> result;

    public IndexMemeResponse(String message, List<Meme> memes) {
        super(message);

        this.result = memes;
    }
}
