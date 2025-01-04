package com.ageulin.mmm.dtos.responses;

import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public class UpdateMemeUploadURLResponse extends BaseResponse {
    private record Result(
            String url,
            ZonedDateTime expiresAt
    ) {}
    private final Result result;

    public UpdateMemeUploadURLResponse(
            String message,
            String url,
            ZonedDateTime expiresAt
    ) {
        super(message);
        this.result = new Result(url, expiresAt);
    }
}