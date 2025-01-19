package com.ageulin.mmm.exceptions;

public class HttpConflictException extends RuntimeException {
  public HttpConflictException(String message) {
    super("Conflict: " + message);
  }
}
