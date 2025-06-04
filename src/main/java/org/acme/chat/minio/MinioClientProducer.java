package org.acme.chat.minio;

import io.minio.MinioClient;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class MinioClientProducer {

    @ConfigProperty(name = "minio.endpoint")
    String endpoint;

    @ConfigProperty(name = "minio.access-key")
    String accessKey;

    @ConfigProperty(name = "minio.secret-key")
    String secretKey;

    @Produces
    @ApplicationScoped
    public MinioClient getMinioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

}
