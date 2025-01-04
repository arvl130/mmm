package com.ageulin.mmm.dtos.responses;

import com.ageulin.mmm.dtos.PublicMeme;
import lombok.Getter;

import java.util.List;

@Getter
public class IndexMemeResponse extends BaseResponse {
    private final List<PublicMeme> result;

    public IndexMemeResponse(String message, List<PublicMeme> memes) {
        super(message);

        this.result = memes;
    }
}
