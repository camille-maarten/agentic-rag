package org.acme.chat.minio;


import dev.langchain4j.data.document.DocumentSource;
import dev.langchain4j.data.document.Metadata;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import jakarta.inject.Inject;

import java.io.InputStream;

import static dev.langchain4j.internal.ValidationUtils.ensureNotBlank;
import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;
import static java.lang.String.format;

public class MinioS3Source implements DocumentSource {

    MinioClient minioClient;

    public static final String SOURCE = "source";

    private InputStream inputStream;
    private String bucket;
    private String key;

    public MinioS3Source(MinioClient minioClient, String bucket, String key) {
        this.minioClient = ensureNotNull(minioClient, "minioClient");
        this.bucket = ensureNotBlank(bucket, "bucket");
        this.key = ensureNotBlank(key, "key");
    }

    @Override
    public InputStream inputStream() {

        try {
            // Retrieve file from MinIO
            inputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .build()
            );
            return inputStream;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Metadata metadata() {
        return Metadata.from(SOURCE, format("s3://%s/%s", bucket, key));
    }
}
