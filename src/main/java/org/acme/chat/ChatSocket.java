package org.acme.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.OpenConnections;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.utils.HttpUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket(path = "/chat/{username}")
@Path("/api/chat")
@ApplicationScoped
public class ChatSocket {

    private final SessionScopedChatBot bot;

    @Inject
    OpenConnections connections;

    @Inject
    ObjectMapper objectMapper;

    // Store active connection IDs
    private final Set<String> activeConnections = ConcurrentHashMap.newKeySet();

    public ChatSocket(SessionScopedChatBot bot) {
        this.bot = bot;
    }

    @OnOpen
    public String onOpen(WebSocketConnection connection) {
        activeConnections.add(connection.id());
        String username = connection.pathParam("username");
        System.out.println("User connected: " + username + " with connection ID: " + connection.id());

        return "Hello " + username + ", how can I help you?";
    }

    @OnTextMessage
    public String onMessage(String message) throws Exception {
        //        return bot.chat(message);
        System.out.println("got socket message: " + message);
        String messageValue = new ObjectMapper().readTree(message).get("message").asText();
        HttpUtils.httpCall("kafka-native-broker-request-received-data", messageValue, null);
        String spell = "In the meantime I got a nice spell for you: \n\n" + bot.magicSpell(message);
        return "I start processing your request, be patient please. " + HttpUtils.toHtmlText(spell);
    }

    @OnClose
    public void onClose(WebSocketConnection connection) {
        activeConnections.remove(connection.id());
    }

    @POST
    @Path("/broadcast")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response broadcastMessage(String messageBody) {
        System.out.println("Got broadcast message: " + messageBody);
        try {
            // Parse the incoming JSON
            ObjectNode messageNode = (ObjectNode) objectMapper.readTree(messageBody);

            // Add timestamp
            messageNode.put("timestamp", Instant.now().toString());

            // Convert back to JSON string
            String enrichedMessage = objectMapper.writeValueAsString(messageNode);

            // Broadcast to all active WebSocket connections using connection IDs
            int successCount = 0;
            int errorCount = 0;

            for (String connectionId : activeConnections) {
                try {
                    Optional<WebSocketConnection> connection = connections.findByConnectionId(connectionId);
                    if (!connection.isEmpty()) {
                        connection.get().sendTextAndAwait(enrichedMessage);
                        successCount++;
                    }
                } catch (Exception e) {
                    errorCount++;
                    // Log the error but continue with other connections
                    System.err.println("Failed to send message to connection " + connectionId + ": " + e.getMessage());
                }
            }

            return Response.ok()
                    .entity("{\"status\":\"Message broadcasted\",\"successful\":" + successCount + ",\"errors\":" + errorCount + "}")
                    .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Failed to process message: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}