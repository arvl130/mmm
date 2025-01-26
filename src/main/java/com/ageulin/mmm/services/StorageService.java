package com.ageulin.mmm.services;

import com.ageulin.mmm.utils.EnvironmentVariableUtils;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@Service
public class StorageService {
    private final String AWS_S3_BUCKET;
    private final String AWS_S3_BUCKET_BASE_URL;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    public StorageService() {
        this.AWS_S3_BUCKET = EnvironmentVariableUtils
            .getenvOrFail("AWS_S3_BUCKET");
        this.AWS_S3_BUCKET_BASE_URL = EnvironmentVariableUtils
            .getenvOrFail("AWS_S3_BUCKET_BASE_URL");

        var awsRegion = System.getenv("AWS_REGION");
        var presignerBuilder = S3Presigner.builder();
        var clientBuilder = S3Client.builder();

        if (null == awsRegion || awsRegion.isEmpty()) {
            presignerBuilder.region(Region.AP_SOUTHEAST_1);
            clientBuilder.region(Region.AP_SOUTHEAST_1);
        }

        this.s3Client = clientBuilder.build();
        this.s3Presigner = presignerBuilder.build();
    }

    public ResponseInputStream<GetObjectResponse> getObject(String key) {
        return s3Client.getObject(builder -> builder
            .bucket(this.AWS_S3_BUCKET)
            .key(key)
            .build()
        );
    }

    public String getObjectURL(String key) {
        return this.AWS_S3_BUCKET_BASE_URL + "/" + key;
    }

    public void deleteObject(String key) {
        this.s3Client.deleteObject(builder -> builder
            .bucket(this.AWS_S3_BUCKET)
            .key(key)
            .build()
        );
    }

    public boolean isExistingObject(String key) {
        try {
            this.s3Client.headObject(builder -> builder
                .bucket(this.AWS_S3_BUCKET)
                .key(key)
                .build()
            );

            return true;
        } catch (NoSuchKeyException ignored) {
            return false;
        }
    }

    public PresignedPutObjectRequest createPresignedPutObjectRequest(String key) {
        var objectRequest = PutObjectRequest.builder()
            .bucket(this.AWS_S3_BUCKET)
            .key(key)
            .build();

        var presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(5))
            .putObjectRequest(objectRequest)
            .build();

        return s3Presigner.presignPutObject(presignRequest);
    }
}
