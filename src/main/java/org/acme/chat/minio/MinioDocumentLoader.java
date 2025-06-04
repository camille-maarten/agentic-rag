package org.acme.chat.minio;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentLoader;
import dev.langchain4j.data.document.DocumentParser;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.messages.Item;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@ApplicationScoped
public class MinioDocumentLoader{

    @Inject
    MinioClient minioClient;

    public Document loadDocument(MinioS3Source source, DocumentParser parser) throws Exception {
        return DocumentLoader.load(source, parser);
    }

    public List<Document> loadDocuments(String bucketName, DocumentParser parser) {
        return StreamSupport.stream(minioClient.listObjects(
                        ListObjectsArgs.builder()
                                .bucket(bucketName)
                                .build()
                ).spliterator(), false)
                .map(result -> {
                    try {
                        return result.get(); // Extracts Item from Result<Item>
                    } catch (Exception e) {
                        throw new RuntimeException("Error retrieving file from MinIO", e);
                    }
                })
                .map(Item::objectName)
                .map(key -> {
                    try {
                        return loadDocument(new MinioS3Source(minioClient, bucketName, key), parser);
                    } catch (Exception e) {
                        throw new RuntimeException("Error loading document: " + key, e);
                    }
                })
                .collect(Collectors.toList());
    }

}
