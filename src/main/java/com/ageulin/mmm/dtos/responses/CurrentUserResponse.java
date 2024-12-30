package com.ageulin.mmm.dtos.responses;

import com.ageulin.mmm.dtos.PublicUser;
import lombok.Getter;

@Getter
public class CurrentUserResponse extends BaseResponse {
    private PublicUser result;

    public CurrentUserResponse(String message, PublicUser publicUser) {
        super(message);
        this.result = publicUser;
    }

    // If there is no user, we just want the `result` field to be serialized as a
    // `null` value (as opposed to `undefined`). So we have this constructor for that.
    public CurrentUserResponse(String message) {
        super(message);
    }
}
