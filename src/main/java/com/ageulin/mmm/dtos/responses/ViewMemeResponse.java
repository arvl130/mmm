package com.ageulin.mmm.dtos.responses;

import com.ageulin.mmm.dtos.PublicMeme;
import lombok.Getter;

@Getter
public class ViewMemeResponse extends BaseResponse {
    private final PublicMeme result;

    public ViewMemeResponse(String message, PublicMeme publicMeme) {
        super(message);

        this.result = publicMeme;
    }
}
