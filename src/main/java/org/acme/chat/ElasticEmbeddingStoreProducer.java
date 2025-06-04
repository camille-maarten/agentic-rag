package org.acme.chat;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.elasticsearch.client.RestClient;

public class ElasticEmbeddingStoreProducer {

    @Inject
    RestClient restClient;

    @Produces
    @ApplicationScoped
    public EmbeddingStore<TextSegment> produceEmbeddingStore() {
        return ElasticsearchEmbeddingStore.builder()
                .restClient(restClient)
                .indexName("quarkus-chat")
                .build();
    }
}
