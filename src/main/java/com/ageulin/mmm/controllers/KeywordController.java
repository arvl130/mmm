package com.ageulin.mmm.controllers;

import com.ageulin.mmm.dtos.responses.KeywordSuggestionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/keywords")
public class KeywordController {
    @GetMapping("/suggestions")
    public ResponseEntity<KeywordSuggestionResponse> getSuggestions(
        @RequestParam("file") MultipartFile file
    ) {
        var fileContentType = file.getContentType();
        if (!isValidContentType(file) || null == fileContentType) {
            throw new UnsupportedMediaTypeStatusException(
                "Only JPEG and PNG files are allowed."
            );
        }

        var prompt =
            """
            You are good at describing images in JSON format. \
            For example: ['frog', 'green', 'sad', 'large eyes']

            Describe the following image in given format.""";

        try (var client = BedrockRuntimeClient.builder().build()) {
            try {
                var imageSource = ImageSource.builder()
                    .bytes(SdkBytes.fromByteArray(file.getBytes()))
                    .build();

                var imageBlock = ImageBlock.builder()
                    .source(imageSource)
                    .format(ImageFormat.JPEG)
                    .build();

                var message = Message.builder()
                    .content(ContentBlock.fromImage(imageBlock))
                    .role(ConversationRole.USER)
                    .build();

                ConverseResponse response = client.converse(request -> request
                        .modelId(System.getenv("AWS_BEDROCK_MODEL_ID"))
                        .messages(message)
                        .inferenceConfig(config -> config
                                .maxTokens(512)
                                .temperature(0.5F)
                                .topP(0.9F)));

                var responseText = response.output().message().content().getFirst().text();

                return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new KeywordSuggestionResponse(
                        "Received reply from AI model.",
                        censorAWSAccountNumber(
                                System.getenv("AWS_BEDROCK_MODEL_ID")
                        ),
                        prompt,
                        file.getContentType(),
                        file.getOriginalFilename(),
                        responseText
                    ));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static String censorAWSAccountNumber(String arn) {
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

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
        MediaType.IMAGE_JPEG_VALUE,
        MediaType.IMAGE_PNG_VALUE
    );

    public static boolean isValidContentType(MultipartFile file) {
        if (null == file || file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        return ALLOWED_CONTENT_TYPES.contains(contentType);
    }
}
