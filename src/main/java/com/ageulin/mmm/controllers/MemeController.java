package com.ageulin.mmm.controllers;

import com.ageulin.mmm.dtos.responses.MemeResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/memes")
public class MemeController {
    @GetMapping
    public ResponseEntity<MemeResponse> index() {
        var response = new MemeResponse("Retrieved memes.", List.of());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
