package se.elias.pdfservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner presigner;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public S3Service(S3Client s3Client, S3Presigner presigner) {
        this.s3Client = s3Client;
        this.presigner = presigner;
    }

    public String uploadAndGetUrl(byte[] data, String filename) {
        String key = "pdfs/" + UUID.randomUUID() + "/" + filename;

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType("application/pdf")
                        .build(),
                RequestBody.fromBytes(data)
        );

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .getObjectRequest(r -> r.bucket(bucketName).key(key))
                .build();

        return presigner.presignGetObject(presignRequest).url().toString();
    }
}