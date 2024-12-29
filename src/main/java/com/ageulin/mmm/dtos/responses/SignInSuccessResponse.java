package com.ageulin.mmm.dtos.responses;

import com.ageulin.mmm.dtos.PublicUser;
import lombok.Getter;

@Getter
public class SignInSuccessResponse extends SignInResponse {
    private final PublicUser result;

    public SignInSuccessResponse(String message, PublicUser publicUser) {
        super(message);
        this.result = publicUser;
    }
}
