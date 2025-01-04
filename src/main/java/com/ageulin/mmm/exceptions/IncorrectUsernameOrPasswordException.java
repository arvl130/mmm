package com.ageulin.mmm.exceptions;

public class IncorrectUsernameOrPasswordException extends RuntimeException {
    public IncorrectUsernameOrPasswordException() {
        super("Incorrect username or password.");
    }
}
