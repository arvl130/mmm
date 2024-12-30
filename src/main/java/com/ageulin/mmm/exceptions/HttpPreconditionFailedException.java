package com.ageulin.mmm.exceptions;

public class HttpPreconditionFailedException extends RuntimeException {
  public HttpPreconditionFailedException(String message) {
    super("Precondition failed: " + message);
  }
}
