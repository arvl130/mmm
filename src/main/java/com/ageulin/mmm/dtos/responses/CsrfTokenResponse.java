package com.ageulin.mmm.dtos.responses;

import lombok.Getter;
import org.springframework.security.web.csrf.CsrfToken;

@Getter
public class CsrfTokenResponse extends BaseResponse {
    private final CsrfToken result;

    public CsrfTokenResponse(String message, CsrfToken csrfToken) {
        super(message);
        this.result = csrfToken;
    }
}
