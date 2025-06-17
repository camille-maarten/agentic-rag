package org.acme.rag;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.function.Supplier;


@ApplicationScoped
public class ElasticAugmentor implements Supplier<RetrievalAugmentor> {

    private final EmbeddingStoreContentRetriever retriever;
    private final EmbeddingStore<TextSegment> embeddingStore;

    ElasticAugmentor(EmbeddingStore<TextSegment> elasticEmbeddingStore, EmbeddingModel embeddingModel) {
        this.embeddingStore = elasticEmbeddingStore;

        retriever = EmbeddingStoreContentRetriever.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(elasticEmbeddingStore)
                .maxResults(5)
                .minScore(0.6) // Add minimum similarity threshold
                .build();
    }

    @Override
    public RetrievalAugmentor get() {
        return DefaultRetrievalAugmentor.builder()
                .contentRetriever(retriever)
                .build();
    }

}
