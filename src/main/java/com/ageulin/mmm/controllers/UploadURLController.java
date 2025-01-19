package com.ageulin.mmm.controllers;

import com.ageulin.mmm.config.SecurityUser;
import com.ageulin.mmm.dtos.responses.StoreMemeUploadURLResponse;
import com.ageulin.mmm.dtos.responses.UpdateMemeUploadURLResponse;
import com.ageulin.mmm.exceptions.HttpNotFoundException;
import com.ageulin.mmm.repositories.MemeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.ZoneId;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/upload-urls")
public class UploadURLController {
    private final String AWS_S3_BUCKET;
    private final MemeRepository memeRepository;

    public UploadURLController(MemeRepository memeRepository) {
        this.AWS_S3_BUCKET = System.getenv("AWS_S3_BUCKET");
        this.memeRepository = memeRepository;
    }

    @GetMapping("/memes")
    public ResponseEntity<StoreMemeUploadURLResponse> getStoreMemeUploadURL() {
        var id = UUID.randomUUID();
        var presignedRequest = createPresignedRequest("memes/" + id);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(
                new StoreMemeUploadURLResponse(
                    "Created upload URL.",
                    presignedRequest.url().toString(),
                    presignedRequest.expiration(),
                    id
                )
            );
    }

    @GetMapping("/memes/{memeId}")
    public ResponseEntity<UpdateMemeUploadURLResponse> getUpdateMemeUploadURL(
        @PathVariable UUID memeId,
        @AuthenticationPrincipal SecurityUser securityUser
    ) {
        var meme = this.memeRepository.findByIdAndUserId(memeId, securityUser.getId())
            .orElseThrow(() -> new HttpNotFoundException("No such meme."));

        var presignedRequest = createPresignedRequest("memes/" + meme.getId());
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(new UpdateMemeUploadURLResponse(
                "Created upload URL.",
                presignedRequest.url().toString(),
                presignedRequest.expiration()
            ));
    }

    @GetMapping("/user/avatar")
    public ResponseEntity<UpdateMemeUploadURLResponse> getUserAvatarUploadURL(
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        var presignedRequest = createPresignedRequest("avatars/" + securityUser.getId());
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(new UpdateMemeUploadURLResponse(
                "Created upload URL.",
                presignedRequest.url().toString(),
                presignedRequest.expiration()
            ));
    }

    private PresignedPutObjectRequest createPresignedRequest(String key) {
        try (var presigner = S3Presigner.create()) {
            var objectRequest = PutObjectRequest.builder()
                .bucket(this.AWS_S3_BUCKET)
                .key(key)
                .build();

            var presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(objectRequest)
                .build();

            return presigner.presignPutObject(presignRequest);
        }
    }
}
