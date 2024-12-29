package com.ageulin.mmm.controllers;

import com.ageulin.mmm.dtos.responses.CsrfResponse;
import com.ageulin.mmm.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
public class CsrfController {
    @GetMapping("/csrf")
    public ResponseEntity<CsrfResponse> csrf(CsrfToken csrfToken) {
        var response = new CsrfResponse(
            "Retrieved CSRF token.",
            csrfToken
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
