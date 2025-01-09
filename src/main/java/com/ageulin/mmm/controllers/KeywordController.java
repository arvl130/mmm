package com.ageulin.mmm.controllers;

import com.ageulin.mmm.dtos.responses.KeywordSuggestionResponse;
import com.ageulin.mmm.exceptions.HttpTooManyRequestsException;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/keywords")
public class KeywordController {
    @PostMapping("/suggestions")
    public ResponseEntity<KeywordSuggestionResponse> getSuggestions(
        @NotNull @RequestParam("file") MultipartFile file
    ) {
        if (file.isEmpty()) {
            throw new ServerWebInputException("Input file is required.");
        }

        var imageFormat = switch (file.getContentType()) {
            case MediaType.IMAGE_PNG_VALUE -> ImageFormat.PNG;
            case MediaType.IMAGE_JPEG_VALUE -> ImageFormat.JPEG;
            case null, default -> throw new UnsupportedMediaTypeStatusException(
                "Only JPEG and PNG files are allowed."
            );
        };

        var prompt = """
         You are an expert at analyzing images.
         Extract the most relevant keywords that best represent the image’s content.
        
         Respond with a comma-separated list of keywords, with no extra text.
         Example: foo, bar, baz""";

        var systemMessage = SystemContentBlock.builder().text(prompt).build();

        try (var client = BedrockRuntimeClient.builder().build()) {
            try {
                var imageSource = ImageSource.builder()
                    .bytes(SdkBytes.fromByteArray(file.getBytes()))
                    .build();

                var imageBlock = ImageBlock.builder()
                    .source(imageSource)
                    .format(imageFormat)
                    .build();

                var imageMessage = Message.builder()
                    .content(ContentBlock.fromImage(imageBlock))
                    .role(ConversationRole.USER)
                    .build();

                ConverseResponse response = client.converse(request -> request
                    .modelId(System.getenv("AWS_BEDROCK_MODEL_ID"))
                    .system(systemMessage)
                    .messages(imageMessage)
                    .inferenceConfig(config -> config
                        .maxTokens(512)
                        .temperature(0.5F)
                        .topP(0.9F)));

                var responseText = response.output().message().content().getFirst().text();
                var keywords = Arrays
                    .stream(responseText.split(","))
                    .map(s -> s.trim().toLowerCase())
                    // Sometimes, the LLM will add a period at the end of the
                    // keyword (possibly because it is the last in the list?).
                    // So we remove it here if that is the case.
                    .map(s -> s.endsWith(".") ? s.substring(0, s.length() - 1) : s)
                    .collect(Collectors.toSet());

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
                        keywords
                    ));
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ThrottlingException e) {
                throw new HttpTooManyRequestsException();
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
}
