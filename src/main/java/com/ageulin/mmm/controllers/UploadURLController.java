package com.ageulin.mmm.controllers;

import com.ageulin.mmm.config.SecurityUser;
import com.ageulin.mmm.dtos.responses.StoreMemeUploadURLResponse;
import com.ageulin.mmm.dtos.responses.UpdateMemeUploadURLResponse;
import com.ageulin.mmm.exceptions.HttpNotFoundException;
import com.ageulin.mmm.repositories.MemeRepository;
import com.ageulin.mmm.services.StorageService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/upload-urls")
@AllArgsConstructor
public class UploadURLController {
    private final MemeRepository memeRepository;
    private final StorageService storageService;

    @GetMapping("/memes")
    public ResponseEntity<StoreMemeUploadURLResponse> getStoreMemeUploadURL() {
        var id = UUID.randomUUID();
        var presignedRequest = this.storageService
            .createPresignedPutObjectRequest("memes/" + id);

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
        var meme = this.memeRepository
            .findByIdAndUserId(memeId, securityUser.getId())
            .orElseThrow(() -> new HttpNotFoundException("No such meme."));

        var presignedRequest =  this.storageService
            .createPresignedPutObjectRequest("memes/" + meme.getId());

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
        var presignedRequest = this.storageService
            .createPresignedPutObjectRequest("avatars/" + securityUser.getId());

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(new UpdateMemeUploadURLResponse(
                "Created upload URL.",
                presignedRequest.url().toString(),
                presignedRequest.expiration()
            ));
    }
}
