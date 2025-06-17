package org.acme.agents.clients;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Arrays;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

@ApplicationScoped
public class DietaryRestrictionsClient {

    @Inject
    ObjectMapper objectMapper;

    private final HttpClient httpClient;

    public DietaryRestrictionsClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public List<String> getDietaryRestrictions() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/tool/dietary-restrictions"))
                .GET()
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP error code: " + response.statusCode());
        }

        // Parse JSON array to List<String>
        return objectMapper.readValue(response.body(),
                new TypeReference<List<String>>() {
                });
    }

    // Alternative method using simple JSON parsing if you don't want Jackson dependency
    public List<String> getDietaryRestrictionsSimple() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/tool/dietary-restrictions"))
                .GET()
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP error code: " + response.statusCode());
        }

        // Simple parsing for JSON array format like ["item1", "item2", "item3"]
        String jsonResponse = response.body().trim();
        if (jsonResponse.startsWith("[") && jsonResponse.endsWith("]")) {
            // Remove brackets and split by comma
            String content = jsonResponse.substring(1, jsonResponse.length() - 1);
            return Arrays.stream(content.split(","))
                    .map(s -> s.trim().replaceAll("\"", "")) // Remove quotes
                    .filter(s -> !s.isEmpty())
                    .toList();
        }

        throw new RuntimeException("Unexpected JSON format: " + jsonResponse);
    }
}