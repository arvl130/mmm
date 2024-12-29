package com.ageulin.mmm.dtos.responses;

import org.springframework.security.web.csrf.CsrfToken;

public record CsrfResponse(
    String message,
    CsrfToken result
) {
}