package org.acme.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.time.Duration;

public class HttpUtils {
    public static void httpCall(
            String brokerName,
            String originalRequest,
            String content
    ){
        try {
            // Create HTTP client
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            // Generate UUIDs for id and traceId
            String traceId = UUID.randomUUID().toString();

            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
            String timestamp = now.format(formatter);

            // Create the JSON body with your specified structure
            String jsonBody = String.format(
                    "{\"messageId\": \"%s\", \"originalRequest\": \"%s\", \"content\": \"%s\", \"timestamp\": \"%s\"}",
                    traceId,
                    originalRequest.replaceAll("\"", "\\\""),
                    content != null ? content.replaceAll("\"", "\\\""): "",
                    timestamp
            );

            // Build the HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://kafka-broker-ingress.knative-eventing.svc.cluster.local/elastic-vector/" + brokerName))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .header("Ce-Id", "my-event-id-123")
                    .header("Ce-Source", "chatbot")
                    .header("Ce-Type", "request.received")
                    .header("Ce-Specversion", "1.0")
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .build();

            // Send the request
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            // Handle the response
            System.out.println("Response Status Code: " + response.statusCode());
            System.out.println("Request Body: " + jsonBody);
            System.out.println("Response Body: " + response.body());
            System.out.println("Trace ID: " + traceId);

        } catch (Exception e) {
            System.err.println("Error sending HTTP request: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static String toHtmlText(String input) {
        if (input == null || input.isEmpty()) return "";

        // Escape HTML special characters
        input = input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");

        // Paragraphs: split on double line breaks and wrap in <p>
        String[] paragraphs = input.split("\\n\\s*\\n");
        StringBuilder html = new StringBuilder();

        html.append("<html><body style=\"font-family:Arial,sans-serif; line-height:1.6; background-color:#fefefe; padding:1em;\">");

        for (String paragraph : paragraphs) {
            String formatted = paragraph
                    .replaceAll("\\*\\*(.*?)\\*\\*", "<strong>$1</strong>")   // bold
                    .replaceAll("\\*(.*?)\\*", "<em>$1</em>")                 // italic
                    .replaceAll("\n", "<br>");                                // preserve single line breaks
            html.append("<p>").append(formatted).append("</p>");
        }

        html.append("</body></html>");
        return html.toString();
    }
}
