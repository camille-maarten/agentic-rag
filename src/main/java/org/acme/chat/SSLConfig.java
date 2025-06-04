package org.acme.chat;

import io.quarkus.elasticsearch.restclient.lowlevel.ElasticsearchClientConfig;
import io.quarkus.logging.Log;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClientBuilder;

import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import io.quarkus.runtime.LaunchMode;

@ElasticsearchClientConfig
public class SSLConfig implements RestClientBuilder.HttpClientConfigCallback {

    private static final String TRUSTSTORE_PATH = "/deployments/config/tls.crt";
    private static final boolean DISABLE_HOSTNAME_VERIFICATION = true;


    @Override
    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
        if (LaunchMode.current() == LaunchMode.DEVELOPMENT) {
            return httpClientBuilder;
        } else {
            try {
                // Load the Elasticsearch CA certificate from resources inside the JAR
                KeyStore trustStore = KeyStore.getInstance("JKS");
                CertificateFactory factory = CertificateFactory.getInstance("X.509");

                // Load the JKS file directly from the filesystem path
                try (InputStream truststoreInputStream = Files.newInputStream(Paths.get(TRUSTSTORE_PATH))) {

                    Certificate trustedCa = factory.generateCertificate(truststoreInputStream);

                    // Create a TrustStore and load the CA certificate
                    trustStore = KeyStore.getInstance("JKS");
                    trustStore.load(null, null);
                    trustStore.setCertificateEntry("ca", trustedCa);
                    Log.infof("Successfully loaded truststore from: {}", TRUSTSTORE_PATH);
                } catch (java.nio.file.NoSuchFileException e) {
                    Log.error("Truststore file not found at filesystem path: {}. Ensure the Secret is mounted correctly.", TRUSTSTORE_PATH, e);
                    throw new RuntimeException("Truststore file not found at filesystem path: " + TRUSTSTORE_PATH, e);
                }

                // Build SSLContext with the TrustStore
                SSLContext sslContext = SSLContexts.custom()
                        .loadTrustMaterial(trustStore, null)
                        .build();

                httpClientBuilder.setSSLContext(sslContext);

                // Disable hostname verification to prevent CN mismatch errors
                if (DISABLE_HOSTNAME_VERIFICATION) {
                    httpClientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
                    Log.warn("Elasticsearch client hostname verification is DISABLED.");
                }

            } catch (Exception e) {
                throw new RuntimeException("Failed to configure Elasticsearch SSL", e);
            }
        }
        return httpClientBuilder;
    }
}
