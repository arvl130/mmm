package com.ageulin.mmm.dtos.responses;

import com.ageulin.mmm.entities.Meme;
import lombok.Getter;

@Getter
public class ViewMemeResponse extends BaseResponse {
    private final Meme result;

    public ViewMemeResponse(String message, Meme meme) {
        super(message);

        this.result = meme;
    }
}
