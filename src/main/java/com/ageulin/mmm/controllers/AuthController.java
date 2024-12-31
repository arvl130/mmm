package com.ageulin.mmm.controllers;

import com.ageulin.mmm.config.UsernameAndPasswordUser;
import com.ageulin.mmm.dtos.PublicUser;
import com.ageulin.mmm.dtos.requests.SignInRequest;
import com.ageulin.mmm.dtos.responses.*;
import com.ageulin.mmm.repositories.UserRepository;
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
import org.springframework.security.web.authentication.RememberMeServices;
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
    private final UserRepository userRepository;
    private final RememberMeServices rememberMeServices;

    @PostMapping("/signin")
    public ResponseEntity<BaseResponse> signIn(
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
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new BaseResponse("Incorrect username or password."));
        }

        var user = this.userRepository.findByEmail(signInRequest.username());
        if (user.isEmpty()) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new BaseResponse("Incorrect username or password."));
        }

        var u = user.get();
        var publicUser = new PublicUser(u.getId(), u.getEmail());

        var context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        securityContextHolderStrategy.setContext(context);
        securityContextRepository.saveContext(context, request, response);
        rememberMeServices.loginSuccess(request, response, authentication);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(new SignInResponse("Sign in success.", publicUser));
    }

    @PostMapping("/signup")
    public ResponseEntity<BaseResponse> signUp() {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(new BaseResponse("Sign up success."));
    }

    @GetMapping("/user")
    public ResponseEntity<CurrentUserResponse> getCurrentUser(Authentication authentication) {
        if (null == authentication || !(authentication.getPrincipal() instanceof UsernameAndPasswordUser usernameAndPasswordUser)) {
            return ResponseEntity
                .status(HttpStatus.OK)
                .body(new CurrentUserResponse("Retrieved current user."));
        }

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(
                new CurrentUserResponse(
                    "Retrieved current user.",
                    new PublicUser(usernameAndPasswordUser.getId(), usernameAndPasswordUser.getUsername())
                )
            );
    }
}
