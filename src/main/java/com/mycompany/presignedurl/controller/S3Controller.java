package com.mycompany.presignedurl.controller;

import com.mycompany.presignedurl.record.PresignedUrlRequest;
import com.mycompany.presignedurl.record.PresignedUrlResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.*;

import java.net.URL;
import java.time.Duration;

@RestController
@RequestMapping("/api/s3")
public class S3Controller {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private String bucketName = "default-bucket";

    public S3Controller() {
        this.s3Client = S3Client.builder()
                                .region(Region.US_EAST_1)
                                .credentialsProvider(ProfileCredentialsProvider.create())
                                .build();
        this.s3Presigner = S3Presigner.builder()
                                      .region(Region.US_EAST_1)
                                      .credentialsProvider(ProfileCredentialsProvider.create())
                                      .build();
    }

    @PostMapping("/get-presigned-url")
    public Mono<ResponseEntity<PresignedUrlResponse>> getPresignedUrl(@RequestBody PresignedUrlRequest request) {
        return Mono.fromCallable(() -> {
            String key = request.fileName();
            PresignedPutObjectRequest presignedRequest =
                    s3Presigner.presignPutObject(
                            r ->
                                    r.signatureDuration(Duration.ofMinutes(5))
                                            .putObjectRequest(go -> go.bucket(bucketName).key(key)));
            PresignedUrlResponse response =
                    new PresignedUrlResponse(presignedRequest.url().toString(), presignedRequest.expiration().toString());
            return ResponseEntity.ok(response);
        }).onErrorResume(ex -> {
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PresignedUrlResponse(null, ex.getMessage())));
        });
    }
}
