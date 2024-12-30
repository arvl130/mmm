package com.ageulin.mmm.exceptions;

public class HttpNotFoundException extends RuntimeException {
  public HttpNotFoundException(String message) {
    super(message);
  }
}
