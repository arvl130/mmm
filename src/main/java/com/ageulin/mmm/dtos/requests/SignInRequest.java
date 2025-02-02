package com.ageulin.mmm.dtos.requests;

public record SignInRequest(
    String email,
    String password
) {
}
