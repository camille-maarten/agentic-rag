package org.acme.rag;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.apache.http.HttpHost;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

@ApplicationScoped
public class ElasticEmbeddingStoreProducer {

    @ConfigProperty(name = "elasticsearch.host", defaultValue = "localhost")
    String elasticsearchHost;

    @ConfigProperty(name = "elasticsearch.port", defaultValue = "9200")
    int elasticsearchPort;

    @ConfigProperty(name = "elasticsearch.scheme", defaultValue = "https")
    String elasticsearchScheme;

    @ConfigProperty(name = "elasticsearch.index.name", defaultValue = "quarkus-chat")
    String indexName;

    @ConfigProperty(name = "elasticsearch.username", defaultValue = "")
    String username;

    @ConfigProperty(name = "elasticsearch.password", defaultValue = "")
    String password;

    @Produces
    @ApplicationScoped
    public EmbeddingStore<TextSegment> produceEmbeddingStore() throws Exception {
        RestClient restClient = restClientWithDisabledCertificates();

        // Build and return the embedding store
        return ElasticsearchEmbeddingStore.builder()
                .restClient(restClient) // Use restClient instead of elasticsearchClient
                .indexName(indexName) // Use configurable index name
                .build();
    }

    private @NotNull RestClient restClientWithDisabledCertificates() throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        // ⚠️ WARNING: This configuration accepts ALL certificates - use only for development!
        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
        SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial(null, acceptingTrustStrategy)
                .build();

        HostnameVerifier noopHostnameVerifier = (String hostname, SSLSession session) -> true;

        // Create RestClient with configurable properties and SSL configuration
        RestClientBuilder builder = RestClient.builder(
                        new HttpHost(elasticsearchHost, elasticsearchPort, elasticsearchScheme))
                .setHttpClientConfigCallback(httpClientBuilder -> {
                    httpClientBuilder
                            .setSSLContext(sslContext)
                            .setSSLHostnameVerifier(noopHostnameVerifier);

                    // Add basic authentication if credentials are provided
                    if (!username.isEmpty() && !password.isEmpty()) {
                        org.apache.http.auth.AuthScope authScope =
                                new org.apache.http.auth.AuthScope(elasticsearchHost, elasticsearchPort);
                        org.apache.http.auth.UsernamePasswordCredentials credentials =
                                new org.apache.http.auth.UsernamePasswordCredentials(username, password);
                        org.apache.http.impl.client.BasicCredentialsProvider credentialsProvider =
                                new org.apache.http.impl.client.BasicCredentialsProvider();
                        credentialsProvider.setCredentials(authScope, credentials);
                        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    }

                    return httpClientBuilder;
                });

        RestClient restClient = builder.build();
        return restClient;
    }
}

// package org.acme.chat;

// import dev.langchain4j.data.segment.TextSegment;
// import dev.langchain4j.store.embedding.EmbeddingStore;
// import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
// import jakarta.enterprise.context.ApplicationScoped;
// import jakarta.enterprise.inject.Produces;
// import jakarta.inject.Inject;
// import org.elasticsearch.client.RestClient;

// public class ElasticEmbeddingStoreProducer {

//     @Inject
//     RestClient restClient;

//     @Produces
//     @ApplicationScoped
//     public EmbeddingStore<TextSegment> produceEmbeddingStore() {
//         return ElasticsearchEmbeddingStore.builder()
//                 .restClient(restClient)
//                 .indexName("quarkus-chat")
//                 .build();
//     }
// }
