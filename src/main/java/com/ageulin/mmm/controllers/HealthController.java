package com.ageulin.mmm.controllers;

import com.ageulin.mmm.dtos.responses.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/up")
public class HealthController {
    @GetMapping
    public ResponseEntity<BaseResponse> health() {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(new BaseResponse("MMM is up and running."));
    }
}
