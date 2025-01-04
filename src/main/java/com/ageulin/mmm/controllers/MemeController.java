package com.ageulin.mmm.controllers;

import com.ageulin.mmm.config.SecurityUser;
import com.ageulin.mmm.dtos.requests.StoreMemeRequest;
import com.ageulin.mmm.dtos.requests.UpdateMemeRequest;
import com.ageulin.mmm.dtos.responses.BaseResponse;
import com.ageulin.mmm.dtos.responses.IndexMemeResponse;
import com.ageulin.mmm.dtos.responses.ViewMemeResponse;
import com.ageulin.mmm.entities.Meme;
import com.ageulin.mmm.exceptions.HttpNotFoundException;
import com.ageulin.mmm.exceptions.HttpPreconditionFailedException;
import com.ageulin.mmm.repositories.MemeRepository;
import com.ageulin.mmm.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/memes")
@AllArgsConstructor
public class MemeController {
    private MemeRepository memeRepository;
    private UserRepository userRepository;

    @Transactional
    @PostMapping
    public ResponseEntity<ViewMemeResponse> store(
        @AuthenticationPrincipal SecurityUser securityUser,
        @RequestBody StoreMemeRequest storeMemeRequest
    ) {
        var user = this.userRepository.findById(securityUser.getId())
            .orElseThrow(() -> new HttpPreconditionFailedException("User has no details."));

        var meme = Meme.builder()
            .user(user)
            .imgUrl(storeMemeRequest.imgUrl())
            .build();

        var savedMeme = this.memeRepository.save(meme);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(new ViewMemeResponse("Created meme.", savedMeme));
    }

    @GetMapping
    public ResponseEntity<IndexMemeResponse> index(
        @AuthenticationPrincipal SecurityUser securityUser
    ) {
        var memes = this.memeRepository.findByUserId(securityUser.getId());

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(new IndexMemeResponse("Retrieved memes.", memes));
    }

    @GetMapping("/{memeId}")
    public ResponseEntity<ViewMemeResponse> view(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID memeId
    ) {
        var meme = this.memeRepository.findByIdAndUserId(memeId, securityUser.getId())
            .orElseThrow(() -> new HttpNotFoundException("No such meme."));

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(new ViewMemeResponse("Retrieved meme.", meme));
    }

    @Transactional
    @PutMapping("/{memeId}")
    public ResponseEntity<ViewMemeResponse> update(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID memeId,
        @RequestBody UpdateMemeRequest updateMemeRequest
    ) {
        var existingMeme = this.memeRepository.findByIdAndUserId(memeId, securityUser.getId())
            .orElseThrow(() -> new HttpNotFoundException("No such meme."));

        var modifiedMeme = Meme.builder()
            .id(existingMeme.getId())
            .user(existingMeme.getUser())
            .imgUrl(updateMemeRequest.imgUrl())
            .build();

        var savedMeme = this.memeRepository.save(modifiedMeme);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(new ViewMemeResponse("Updated meme.", savedMeme));
    }

    @Transactional
    @DeleteMapping("/{memeId}")
    public ResponseEntity<BaseResponse> destroy(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID memeId
    ) {
        this.memeRepository.findByIdAndUserId(memeId, securityUser.getId())
            .orElseThrow(() -> new HttpNotFoundException("No such meme."));

        this.memeRepository.deleteById(memeId);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(new BaseResponse("Deleted meme."));
    }
}
