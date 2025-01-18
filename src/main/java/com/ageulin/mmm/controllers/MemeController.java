package com.ageulin.mmm.controllers;

import com.ageulin.mmm.config.SecurityUser;
import com.ageulin.mmm.dtos.PublicMeme;
import com.ageulin.mmm.dtos.requests.StoreMemeRequest;
import com.ageulin.mmm.dtos.requests.UpdateMemeRequest;
import com.ageulin.mmm.dtos.responses.BaseResponse;
import com.ageulin.mmm.dtos.responses.IndexMemeResponse;
import com.ageulin.mmm.dtos.responses.ViewMemeResponse;
import com.ageulin.mmm.entities.Keyword;
import com.ageulin.mmm.entities.Meme;
import com.ageulin.mmm.entities.MemeEmbedding;
import com.ageulin.mmm.exceptions.HttpNotFoundException;
import com.ageulin.mmm.exceptions.HttpPreconditionFailedException;
import com.ageulin.mmm.mappers.KeywordMapper;
import com.ageulin.mmm.repositories.KeywordRepository;
import com.ageulin.mmm.repositories.MemeEmbeddingRepository;
import com.ageulin.mmm.repositories.MemeRepository;
import com.ageulin.mmm.repositories.UserRepository;
import com.ageulin.mmm.services.LlmService;
import com.ageulin.mmm.mappers.StringMapper;
import jakarta.annotation.Nullable;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/memes")
public class MemeController {
    private final String AWS_S3_BUCKET;
    private final String AWS_S3_BUCKET_BASE_URL;
    private final MemeRepository memeRepository;
    private final UserRepository userRepository;
    private final KeywordRepository keywordRepository;
    private final MemeEmbeddingRepository memeEmbeddingRepository;
    private final LlmService llmService;


    public MemeController(
            MemeRepository memeRepository,
            UserRepository userRepository,
            KeywordRepository keywordRepository,
            MemeEmbeddingRepository memeEmbeddingRepository,
            LlmService llmService
    ) {
        this.memeEmbeddingRepository = memeEmbeddingRepository;
        this.llmService = llmService;
        this.AWS_S3_BUCKET = System.getenv("AWS_S3_BUCKET");
        this.AWS_S3_BUCKET_BASE_URL = System.getenv("AWS_S3_BUCKET_BASE_URL");
        this.memeRepository = memeRepository;
        this.userRepository = userRepository;
        this.keywordRepository = keywordRepository;
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

        var keywords = storeMemeRequest
            .keywords()
            .stream()
            .map(name -> this.keywordRepository
                // Get item.
                .findByName(name)
                // If not present, then create it.
                .orElseGet(() -> this.keywordRepository
                    .save(Keyword.builder().name(name).build())
                )
            ).collect(Collectors.toSet());

        var meme = Meme.builder()
            .id(storeMemeRequest.id())
            .user(user)
            .keywords(keywords)
            .build();

        var savedMeme = this.memeRepository.save(meme);
        if (!keywords.isEmpty()) {
            var searchable = String.join(
                " ",
                keywords
                    .stream()
                    .map(Keyword::getName)
                    .collect(Collectors.toSet())
            );

            this.memeRepository.updateSearchableByIdAndUserId(
                storeMemeRequest.id(),
                securityUser.getId(),
                searchable
            );

            var chunks = StringMapper.toChunks(
                String.join(" ", storeMemeRequest.keywords()), 2048
            );
            var generatedEmbeddings = this.llmService
                .generateEmbeddings(chunks).embeddings();

            for (int i = 0; i < generatedEmbeddings.size(); i++) {
                var memeEmbedding = MemeEmbedding.builder()
                    .text(chunks.get(i))
                    .embedding(generatedEmbeddings.get(i))
                    .meme(savedMeme)
                    .build();

                this.memeEmbeddingRepository.save(memeEmbedding);
            }
        }

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
        @AuthenticationPrincipal SecurityUser securityUser,
        @RequestParam(name = "q", required = false) String searchTerm,
        @RequestParam(name = "mode", required = false) String searchMode
    ) {
        var memes = getMemes(securityUser.getId(), searchTerm, searchMode);
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

    private List<Meme> getMemes(
        UUID userId,
        @Nullable String searchTerm,
        @Nullable String searchMode
    ) {
        if (null == searchTerm || "".equalsIgnoreCase(searchTerm)) {
            return this.memeRepository.findByUserId(userId);
        } else {
            return switch (searchMode) {
                case "FULL_TEXT" -> this.memeRepository
                    .websearch(searchTerm);
                case "SEMANTIC" -> {
                    var searchEmbedding = this.llmService
                        .generateEmbeddings(List.of(searchTerm))
                        .embeddings()
                        .getFirst();

                    yield this.memeRepository
                        .findSimilar(searchEmbedding, 50);
                }
                case null, default -> this.memeRepository
                    .findDistinctByUserIdAndKeywords_NameContaining(userId, searchTerm);
            };
        }
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
        @Valid @RequestBody UpdateMemeRequest updateMemeRequest
    ) {
        var existingMeme = this.memeRepository.findByIdAndUserId(memeId, securityUser.getId())
            .orElseThrow(() -> new HttpNotFoundException("No such meme."));

        var keywords = updateMemeRequest
            .keywords()
            .stream()
            .map(name -> this.keywordRepository
                .findByName(name)
                .orElseGet(() -> this.keywordRepository
                    .save(Keyword.builder().name(name).build())
                )
            ).collect(Collectors.toSet());

        var modifiedMeme = Meme.builder()
            .id(existingMeme.getId())
            .user(existingMeme.getUser())
            .keywords(keywords)
            .build();

        var savedMeme = this.memeRepository.save(modifiedMeme);
        if (!updateMemeRequest.keywords().isEmpty()) {
            var searchable = String.join(" ", updateMemeRequest.keywords());
            this.memeRepository.updateSearchableByIdAndUserId(
                savedMeme.getId(),
                securityUser.getId(),
                searchable
            );

            var chunks = StringMapper.toChunks(
                String.join(" ", updateMemeRequest.keywords()),
                2048
            );
            var generatedEmbeddings = this.llmService
                .generateEmbeddings(chunks)
                .embeddings();

            this.memeEmbeddingRepository.deleteByMemeId(memeId);

            for (int i = 0; i < generatedEmbeddings.size(); i++) {
                var memeEmbedding = MemeEmbedding.builder()
                    .text(chunks.get(i))
                    .embedding(generatedEmbeddings.get(i))
                    .meme(savedMeme)
                    .build();

                this.memeEmbeddingRepository.save(memeEmbedding);
            }
        }

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

        try (var s3Client = S3Client.builder().build()) {
            s3Client.deleteObject(builder -> builder
                .bucket(this.AWS_S3_BUCKET)
                .key("memes/" + memeId)
                .build()
            );
        }

        this.memeRepository.deleteById(memeId);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(new BaseResponse("Deleted meme."));
    }
}
