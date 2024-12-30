package com.ageulin.mmm.dtos.responses;

import com.ageulin.mmm.dtos.PublicUser;
import lombok.Getter;

@Getter
public class SignInResponse extends BaseResponse {
    private final PublicUser result;

    public SignInResponse(String message, PublicUser publicUser) {
        super(message);
        this.result = publicUser;
    }
}
