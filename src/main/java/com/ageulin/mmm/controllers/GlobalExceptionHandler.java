package com.ageulin.mmm.controllers;

import com.ageulin.mmm.dtos.responses.BaseResponse;
import com.ageulin.mmm.exceptions.HttpNotFoundException;
import com.ageulin.mmm.exceptions.HttpPreconditionFailedException;
import com.ageulin.mmm.exceptions.HttpTooManyRequestsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(HttpNotFoundException.class)
    public ResponseEntity<BaseResponse> handleNotFoundException(HttpNotFoundException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new BaseResponse(ex.getMessage()));
    }

    @ExceptionHandler(HttpPreconditionFailedException.class)
    public ResponseEntity<BaseResponse> handlePreconditionFailedException(HttpPreconditionFailedException ex) {
        return ResponseEntity
            .status(HttpStatus.PRECONDITION_FAILED)
            .body(new BaseResponse(ex.getMessage()));
    }

    @ExceptionHandler(HttpTooManyRequestsException.class)
    public ResponseEntity<BaseResponse> handleTooManyRequestsException(HttpTooManyRequestsException ex) {
        return ResponseEntity
            .status(HttpStatus.TOO_MANY_REQUESTS)
            .body(new BaseResponse(ex.getMessage()));
    }
}
