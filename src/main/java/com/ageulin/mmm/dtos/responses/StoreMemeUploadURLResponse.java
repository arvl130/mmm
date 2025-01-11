package com.ageulin.mmm.dtos.responses;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class StoreMemeUploadURLResponse extends BaseResponse {
    private record Result(
        String url,
        Instant expiresAt,
        UUID id
    ) {}
    private final Result result;

    public StoreMemeUploadURLResponse(
        String message,
        String url,
        Instant expiresAt,
        UUID id
    ) {
        super(message);
        this.result = new Result(url, expiresAt, id);
    }
}