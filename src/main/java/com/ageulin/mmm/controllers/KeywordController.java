package com.ageulin.mmm.controllers;

import com.ageulin.mmm.config.SecurityUser;
import com.ageulin.mmm.dtos.responses.KeywordSuggestionResponse;
import com.ageulin.mmm.exceptions.HttpTooManyRequestsException;
import com.ageulin.mmm.repositories.MemeRepository;
import com.ageulin.mmm.services.LlmService;
import com.ageulin.mmm.services.StorageService;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ServerErrorException;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.model.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class KeywordController {
    private static final String prompt = """
        You are an expert at analyzing images.
        Extract the most relevant keywords that best represent the image's content.

        Respond with a comma-separated list of keywords, with no extra text.
        Example: foo, bar, baz
    """;

    private final MemeRepository memeRepository;
    private final StorageService storageService;
    private final LlmService llmService;

    public KeywordController(
        MemeRepository memeRepository,
        StorageService storageService,
        LlmService llmService
    ) {
        this.memeRepository = memeRepository;
        this.storageService = storageService;
        this.llmService = llmService;
    }

    @PostMapping("/keywords/suggestions")
    public ResponseEntity<KeywordSuggestionResponse> getSuggestions(
        @NotNull @RequestParam("file") MultipartFile file
    ) {
        if (file.isEmpty()) {
            throw new ServerWebInputException("Input file is required.");
        }

        try {
            var keywords = getKeywordSuggestions(file.getBytes(), file.getContentType());
            return ResponseEntity
                .status(HttpStatus.OK)
                .body(new KeywordSuggestionResponse(
                    "Received reply from AI model.",
                    censorAWSAccountNumber(this.llmService.getModelId()),
                    prompt,
                    file.getContentType(),
                    file.getOriginalFilename(),
                    keywords
                ));
        } catch (IOException e) {
            throw new ServerErrorException("Could not get file bytes.", e);
        }
    }

    @GetMapping("/memes/{id}/keyword-suggestions")
    public ResponseEntity<KeywordSuggestionResponse> getEditSuggestions(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id
    )
    {
        var meme = this.memeRepository
            .findByIdAndUserId(id, securityUser.getId())
            .orElseThrow(() -> new ServerWebInputException("No such meme."));

        try {
            var inputStream = this.storageService.getObject("memes/" + meme.getId());
            var imageBytes = inputStream.readAllBytes();
            var imageContentType = inputStream.response().contentType();
            var keywords = getKeywordSuggestions(imageBytes, imageContentType);

            return ResponseEntity
                .status(HttpStatus.OK)
                .body(new KeywordSuggestionResponse(
                    "Received reply from AI model.",
                    censorAWSAccountNumber(this.llmService.getModelId()),
                    prompt,
                    imageContentType,
                    keywords
                ));

        } catch (IOException e) {
            throw new ServerErrorException("Meme could not be retrieved.", e);
        }
    }

    private Set<String> getKeywordSuggestions(
        byte[] bytes, @Nullable String contentType
    ) {
        var imageFormat = switch (contentType) {
            case MediaType.IMAGE_PNG_VALUE -> ImageFormat.PNG;
            case MediaType.IMAGE_JPEG_VALUE -> ImageFormat.JPEG;
            case null, default -> throw new ServerErrorException(
                "Uploaded meme has illegal content type.",
                new UnsupportedMediaTypeStatusException(
                    "Only JPEG and PNG files are allowed."
                )
            );
        };

        var systemMessage = SystemContentBlock.builder().text(prompt).build();
        var imageSource = ImageSource.builder()
                .bytes(SdkBytes.fromByteArray(bytes))
                .build();

        var imageBlock = ImageBlock.builder()
                .source(imageSource)
                .format(imageFormat)
                .build();

        var imageMessage = Message.builder()
                .content(ContentBlock.fromImage(imageBlock))
                .role(ConversationRole.USER)
                .build();

        try {
            ConverseResponse response = this.llmService
                .generateResponse(systemMessage, imageMessage);

            var content = response.output().message().content();
            if (content.isEmpty()) {
                return Set.of();
            } else {
                return Arrays
                    .stream(content.getFirst().text().split(","))
                    .map(s -> s.trim().toLowerCase())
                    // Sometimes, the LLM will add a period at the end of the
                    // keyword (possibly because it is the last in the list?).
                    // So we remove it here if that is the case.
                    .map(s -> s.endsWith(".") ? s.substring(0, s.length() - 1) : s)
                    .collect(Collectors.toSet());
            }
        } catch (ThrottlingException e) {
            throw new HttpTooManyRequestsException();
        }
    }

    private static String censorAWSAccountNumber(String arn) {
        // Split the ARN into segments by colons
        String[] segments = arn.split(":");

        // Ensure the ARN has at least 6 segments to be valid
        if (segments.length >= 6) {
            // Replace the account number (4th segment) with asterisks
            segments[4] = "****";
        } else {
            throw new IllegalArgumentException("Invalid ARN format.");
        }

        // Rejoin the segments to reconstruct the censored ARN
        return String.join(":", segments);
    }
}
