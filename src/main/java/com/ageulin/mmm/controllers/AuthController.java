package com.ageulin.mmm.controllers;

import com.ageulin.mmm.config.SecurityUser;
import com.ageulin.mmm.config.UsernameAndPasswordUser;
import com.ageulin.mmm.dtos.PublicUser;
import com.ageulin.mmm.dtos.requests.SignInRequest;
import com.ageulin.mmm.dtos.requests.UpdateCurrentUserEmailRequest;
import com.ageulin.mmm.dtos.requests.UpdateCurrentUserPasswordRequest;
import com.ageulin.mmm.dtos.responses.*;
import com.ageulin.mmm.exceptions.HttpConflictException;
import com.ageulin.mmm.exceptions.HttpPreconditionFailedException;
import com.ageulin.mmm.exceptions.IncorrectUsernameOrPasswordException;
import com.ageulin.mmm.repositories.UserRepository;
import com.ageulin.mmm.services.StorageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.session.jdbc.JdbcIndexedSessionRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final SecurityContextHolderStrategy securityContextHolderStrategy
        = SecurityContextHolder.getContextHolderStrategy();
    private final SecurityContextRepository securityContextRepository =
        new HttpSessionSecurityContextRepository();
    private final JdbcIndexedSessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RememberMeServices rememberMeServices;
    private final StorageService storageService;

    public AuthController(
        JdbcIndexedSessionRepository sessionRepository,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        UserRepository userRepository,
        RememberMeServices rememberMeServices,
        StorageService storageService
    ) {
        this.storageService = storageService;
        this.sessionRepository = sessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.rememberMeServices = rememberMeServices;
    }

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
            throw new IncorrectUsernameOrPasswordException();
        }

        var user = this.userRepository.findByEmail(signInRequest.username())
            .orElseThrow(IncorrectUsernameOrPasswordException::new);

        var publicUser = new PublicUser(
            user.getId(),
            user.getEmail(),
            user.getHasAvatar()
                ? this.storageService.getObjectURL("avatars/" + user.getId())
                : null
        );

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

        var optionalUser = this.userRepository.findById(usernameAndPasswordUser.getId());
        if (optionalUser.isEmpty()) {
            return ResponseEntity
                .status(HttpStatus.OK)
                .body(new CurrentUserResponse("Retrieved current user."));
        }

        var user = optionalUser.get();
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(
                new CurrentUserResponse(
                    "Retrieved current user.",
                    new PublicUser(
                        user.getId(),
                        user.getEmail(),
                        user.getHasAvatar()
                            ? this.storageService.getObjectURL("avatars/" + user.getId())
                            : null
                    )
                )
            );
    }

    @Transactional
    @PostMapping("/user/avatar")
    public ResponseEntity<BaseResponse> storeCurrentUserAvatar(
        @AuthenticationPrincipal SecurityUser securityUser
    ) {
        if (null == securityUser) {
            throw new HttpPreconditionFailedException("No user found.");
        }

        var user = this.userRepository.findById(securityUser.getId())
            .orElseThrow(() -> new HttpPreconditionFailedException("No user found."));

        user.setHasAvatar(true);
        this.userRepository.save(user);

        return ResponseEntity
            .ok(new BaseResponse("Created avatar."));
    }

    @Transactional
    @DeleteMapping("/user/avatar")
    public ResponseEntity<BaseResponse> destroyCurrentUserAvatar(
        @AuthenticationPrincipal SecurityUser securityUser
    ) {
        if (null == securityUser) {
            throw new HttpPreconditionFailedException("No user found.");
        }

        var user = this.userRepository.findById(securityUser.getId())
            .orElseThrow(() -> new HttpPreconditionFailedException("No user found."));

        this.storageService.deleteObject("avatars/" + securityUser.getId());

        user.setHasAvatar(false);
        this.userRepository.save(user);

        return ResponseEntity
            .ok(new BaseResponse("Deleted avatar."));
    }

    @Transactional
    @PatchMapping("/user/email")
    public ResponseEntity<BaseResponse> updateCurrentUserEmail(
        Authentication authentication,
        HttpServletRequest request,
        HttpServletResponse response,
        @AuthenticationPrincipal UsernameAndPasswordUser usernameAndPasswordUser,
        @Valid @RequestBody UpdateCurrentUserEmailRequest updateRequest
    ) {
        if (null == usernameAndPasswordUser) {
            throw new HttpPreconditionFailedException("No user found.");
        }

        var user = this.userRepository.findById(usernameAndPasswordUser.getId())
            .orElseThrow(() -> new HttpPreconditionFailedException("No user found."));

        if (this.userRepository.existsByEmail(updateRequest.newEmail())) {
            throw new HttpConflictException("Email is already in use.");
        }

        var currentSessionId = request.getSession().getId();
        var staleSessionIds = this.sessionRepository.findByPrincipalName(
            usernameAndPasswordUser.getUsername()
        ).keySet().stream().filter((s -> !s.equals(currentSessionId)));

        user.setEmail(updateRequest.newEmail());

        var updatedPrincipal = new UsernameAndPasswordUser(
            usernameAndPasswordUser.getId(),
            updateRequest.newEmail(),
            usernameAndPasswordUser.getPassword(),
            usernameAndPasswordUser.getAuthorities()
        );

        var updatedAuthentication = new PreAuthenticatedAuthenticationToken(
            updatedPrincipal,
            authentication.getCredentials(),
            authentication.getAuthorities()
        );

        var context = SecurityContextHolder.getContext();
        context.setAuthentication(updatedAuthentication);

        this.userRepository.save(user);
        this.securityContextRepository.saveContext(context, request, response);
        this.rememberMeServices.loginSuccess(request, response, updatedAuthentication);

        staleSessionIds.forEach((this.sessionRepository::deleteById));

        return ResponseEntity
            .ok(new BaseResponse("Updated email."));
    }

    @Transactional
    @PatchMapping("/user/password")
    public ResponseEntity<BaseResponse> updateCurrentUserPassword(
        @AuthenticationPrincipal SecurityUser securityUser,
        @Valid @RequestBody UpdateCurrentUserPasswordRequest updateRequest
    ) {
        if (null == securityUser) {
            throw new HttpPreconditionFailedException("No user found.");
        }

        var user = this.userRepository.findById(securityUser.getId())
                .orElseThrow(() -> new HttpPreconditionFailedException("No user found."));

        var isCorrectPasswordPresented = this.passwordEncoder
            .matches(updateRequest.oldPassword(), user.getPassword());

        if (!isCorrectPasswordPresented) {
            throw new IncorrectUsernameOrPasswordException();
        }

        user.setPassword(this.passwordEncoder.encode(updateRequest.newPassword()));

        this.userRepository.save(user);

        return ResponseEntity
            .ok(new BaseResponse("Updated password."));
    }

    @ExceptionHandler(IncorrectUsernameOrPasswordException.class)
    public ResponseEntity<BaseResponse> handleIncorrectUsernameOrPasswordError(
        IncorrectUsernameOrPasswordException ex
    ) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new BaseResponse(ex.getMessage()));
    }
}
