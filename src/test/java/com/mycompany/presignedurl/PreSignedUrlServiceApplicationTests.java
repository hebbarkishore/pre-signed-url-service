package com.mycompany.presignedurl;

import com.mycompany.presignedurl.controller.S3Controller;
import com.mycompany.presignedurl.record.PresignedUrlRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class S3ControllerTest {

    @InjectMocks
    private S3Controller s3Controller;

    @Mock
    private S3Presigner s3Presigner;

    @Autowired
    private WebTestClient webTestClient;

    private static final String filaName = "fileName";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        webTestClient = WebTestClient.bindToController(s3Controller).build();
    }

    @Test
    public void testGetPresignedUrlSuccess() {
        ReflectionTestUtils.setField(
                s3Controller, "bucketName", "your-bucket");
        PresignedUrlRequest request = new PresignedUrlRequest("test-file.mp4", "video/mp4");
        webTestClient.post()
                .uri("/api/s3/get-presigned-url")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), PresignedUrlRequest.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.presignedUrl").value(msg -> {
                    assertTrue(msg.toString().contains("https://your-bucket.s3.amazonaws.com/test-file.mp4"));
                })
                .jsonPath("$.expirationTime").exists();
    }

    @Test
    public void testGetPresignedUrlFailure() {
        ReflectionTestUtils.setField(
                s3Controller, "bucketName", null);
        PresignedUrlRequest request = new PresignedUrlRequest("myvideo.mp4", "invalid-content-type");
        webTestClient.post()
                .uri("/api/s3/get-presigned-url")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), PresignedUrlRequest.class)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.presignedUrl").isEmpty();
    }
}

