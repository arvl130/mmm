package com.ageulin.mmm.controllers;

import com.ageulin.mmm.dtos.responses.HealthResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/up")
public class HealthController {
    @GetMapping
    public ResponseEntity<HealthResponse> health() {
        var response = new HealthResponse("MMM is up and running.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
