package com.ageulin.mmm.exceptions;

public class MissingOrInvalidEnvironmentVariableException extends RuntimeException {
  public MissingOrInvalidEnvironmentVariableException(String variableName) {
    super("Missing or invalid value for environment variable: " + variableName);
  }
}
