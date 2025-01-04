package com.ageulin.mmm.controllers;

import com.ageulin.mmm.config.SecurityUser;
import com.ageulin.mmm.dtos.PublicMeme;
import com.ageulin.mmm.dtos.requests.StoreMemeRequest;
import com.ageulin.mmm.dtos.requests.UpdateMemeRequest;
import com.ageulin.mmm.dtos.responses.BaseResponse;
import com.ageulin.mmm.dtos.responses.IndexMemeResponse;
import com.ageulin.mmm.dtos.responses.ViewMemeResponse;
import com.ageulin.mmm.entities.Meme;
import com.ageulin.mmm.exceptions.HttpNotFoundException;
import com.ageulin.mmm.exceptions.HttpPreconditionFailedException;
import com.ageulin.mmm.mappers.KeywordMapper;
import com.ageulin.mmm.repositories.MemeRepository;
import com.ageulin.mmm.repositories.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/memes")
public class MemeController {
    private final String AWS_S3_BUCKET;
    private final String AWS_S3_BUCKET_BASE_URL;
    private final MemeRepository memeRepository;
    private final UserRepository userRepository;

    public MemeController(MemeRepository memeRepository, UserRepository userRepository) {
        this.AWS_S3_BUCKET = System.getenv("AWS_S3_BUCKET");
        this.AWS_S3_BUCKET_BASE_URL = System.getenv("AWS_S3_BUCKET_BASE_URL");
        this.memeRepository = memeRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    @PostMapping
    public ResponseEntity<ViewMemeResponse> store(
        @AuthenticationPrincipal SecurityUser securityUser,
        @Valid @RequestBody StoreMemeRequest storeMemeRequest
    ) {
        var user = this.userRepository.findById(securityUser.getId())
            .orElseThrow(() -> new HttpPreconditionFailedException("User has no details."));

        if (this.memeRepository.existsById(storeMemeRequest.id())) {
            throw new HttpPreconditionFailedException("Meme already exists.");
        }

        try (var s3Client = S3Client.builder().build()) {
            s3Client.headObject(builder -> builder
                .bucket(this.AWS_S3_BUCKET)
                .key("memes/" + storeMemeRequest.id())
                .build()
            );
        } catch (NoSuchKeyException ignored) {
            throw new HttpPreconditionFailedException("No image was uploaded.");
        }

        var meme = Meme.builder()
            .id(storeMemeRequest.id())
            .user(user)
            .keywords(List.of())
            .build();

        var savedMeme = this.memeRepository.save(meme);
        var publicMeme = new PublicMeme(
            savedMeme.getId(),
            this.AWS_S3_BUCKET_BASE_URL + "/memes/" + savedMeme.getId(),
            savedMeme.getKeywords()
                .stream()
                .map(KeywordMapper::toPublic).toList()
        );

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(new ViewMemeResponse("Created meme.", publicMeme));
    }

    @GetMapping
    public ResponseEntity<IndexMemeResponse> index(
        @AuthenticationPrincipal SecurityUser securityUser
    ) {
        var memes = this.memeRepository.findByUserId(securityUser.getId());
        var publicMemes = memes
            .stream()
            .map(meme ->
                new PublicMeme(
                    meme.getId(),
                    this.AWS_S3_BUCKET_BASE_URL + "/memes/" + meme.getId(),
                    meme.getKeywords()
                        .stream()
                        .map(KeywordMapper::toPublic).toList()
                )
            ).toList();

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(new IndexMemeResponse("Retrieved memes.", publicMemes));
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
            .body(
                new ViewMemeResponse(
                    "Retrieved meme.",
                    new PublicMeme(
                        meme.getId(),
                        this.AWS_S3_BUCKET_BASE_URL + "/memes/" + meme.getId(),
                        meme.getKeywords()
                            .stream()
                            .map(KeywordMapper::toPublic).toList()
                    )
                )
            );
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
            .keywords(existingMeme.getKeywords())
            .build();

        var savedMeme = this.memeRepository.save(modifiedMeme);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(
                new ViewMemeResponse(
                    "Updated meme.",
                    new PublicMeme(
                        savedMeme.getId(),
                        this.AWS_S3_BUCKET_BASE_URL + "/memes/" + savedMeme.getId(),
                        savedMeme.getKeywords()
                            .stream()
                            .map(KeywordMapper::toPublic).toList()
                    )
                )
            );
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
