package com.ageulin.mmm.dtos.responses;

import lombok.Getter;

import java.time.Instant;

@Getter
public class UpdateMemeUploadURLResponse extends BaseResponse {
    private record Result(
            String url,
            Instant expiresAt
    ) {}
    private final Result result;

    public UpdateMemeUploadURLResponse(
            String message,
            String url,
            Instant expiresAt
    ) {
        super(message);
        this.result = new Result(url, expiresAt);
    }
}