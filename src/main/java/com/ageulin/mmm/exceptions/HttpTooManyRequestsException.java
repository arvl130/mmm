package com.ageulin.mmm.exceptions;

public class HttpTooManyRequestsException extends RuntimeException {
  public HttpTooManyRequestsException() {
    super("Too many requests: Please wait before trying again.");
  }
}
