package com.ageulin.mmm.controllers;

import com.ageulin.mmm.config.SecurityUser;
import com.ageulin.mmm.dtos.PublicUser;
import com.ageulin.mmm.dtos.requests.SignInRequest;
import com.ageulin.mmm.dtos.responses.CurrentUserResponse;
import com.ageulin.mmm.dtos.responses.SignInResponse;
import com.ageulin.mmm.dtos.responses.SignInSuccessResponse;
import com.ageulin.mmm.dtos.responses.SignUpResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {
    private final SecurityContextHolderStrategy securityContextHolderStrategy
        = SecurityContextHolder.getContextHolderStrategy();
    private final SecurityContextRepository securityContextRepository =
        new HttpSessionSecurityContextRepository();

    private final AuthenticationManager authenticationManager;

    @PostMapping("/signin")
    public ResponseEntity<SignInResponse> signIn(
        @RequestBody SignInRequest signInRequest,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        var token = UsernamePasswordAuthenticationToken
            .unauthenticated(
                signInRequest.username(),
                signInRequest.password()
            );

        var authentication = authenticationManager.authenticate(token);
        if (!authentication.isAuthenticated()) {
            var signInResponse = new SignInResponse("Sign in failed.");

            return new ResponseEntity<>(signInResponse, HttpStatus.BAD_REQUEST);
        }

        var principal = authentication.getPrincipal();
        if (principal instanceof SecurityUser securityUser) {
            var context = securityContextHolderStrategy.createEmptyContext();
            context.setAuthentication(authentication);
            securityContextHolderStrategy.setContext(context);
            securityContextRepository.saveContext(context, request, response);

            var user = new PublicUser(securityUser.getId(), securityUser.getUsername());
            var signInResponse = new SignInSuccessResponse("Sign in success.", user);
            return new ResponseEntity<>(signInResponse, HttpStatus.OK);
        } else {
            var signInResponse = new SignInResponse("Sign in failed.");

            return new ResponseEntity<>(signInResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp() {
        var response = new SignUpResponse("Sign up success.");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/user")
    public ResponseEntity<CurrentUserResponse> getCurrentUser(Authentication authentication) {
        if (null == authentication) {
            var response = new CurrentUserResponse("Retrieved current user.", null);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            if (authentication.getPrincipal() instanceof SecurityUser securityUser) {
                var response = new CurrentUserResponse(
                    "Retrieved current user.",
                    new PublicUser(securityUser.getId(), securityUser.getUsername())
                );

                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                var response = new CurrentUserResponse("Retrieved current user.", null);

                return new ResponseEntity<>(response, HttpStatus.OK);
            }
        }
    }
}
