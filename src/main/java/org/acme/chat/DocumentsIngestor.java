package org.acme.chat;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.IngestionResult;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.acme.chat.minio.MinioDocumentLoader;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;

import static dev.langchain4j.data.document.splitter.DocumentSplitters.recursive;


@ApplicationScoped
public class DocumentsIngestor {

    @ConfigProperty(name = "minio.bucket-name")
    String bucketName;

    @Inject
    EmbeddingModel embeddingModel;

    @Inject
    EmbeddingStore<TextSegment> elasticEmbeddingStore;

    @Inject
    MinioClient minioClient;

    @Inject
    MinioDocumentLoader minioDocumentLoader;

    public void ingest(@Observes StartupEvent event) {
        Log.infof("Ingesting documents...");
        Log.infof("Embedding Store: " + elasticEmbeddingStore.getClass().getSimpleName());
        Log.infof("Embedding Model: " + embeddingModel.getClass().getSimpleName());
        Log.infof("Initializing bucket: " + bucketName);


        boolean found = false;
        try {
            found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());

            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            } else {
                Log.infof("Bucket already exists: " + bucketName);
            }
        } catch (Exception e) {
            Log.error("Could not initialize bucket", e);
        }

        List<Document> documents = null;
        documents = minioDocumentLoader.loadDocuments(bucketName, new ApacheTikaDocumentParser());

        Log.infof("Total documents loaded by Apache Tika: " + documents.size());
        if (!documents.isEmpty()) {
        var ingestor = EmbeddingStoreIngestor.builder()
                .embeddingStore(elasticEmbeddingStore)
                .embeddingModel(embeddingModel)
                .documentSplitter(recursive(50, 0))
                .build();

            IngestionResult result = ingestor.ingest(documents);
            Log.infof("Tokens count: " + result.tokenUsage());
            Log.infof("Ingested %d documents.%n", documents.size());
        }

    }
}
