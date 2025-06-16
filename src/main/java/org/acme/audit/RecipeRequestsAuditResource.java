package org.acme.audit;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Path("/api/audit")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RecipeRequestsAuditResource {

    // In-memory storage for audit messages by topic
    private final ConcurrentMap<String, List<AuditMessage>> auditStorage = new ConcurrentHashMap<>();

    // Initialize storage for each topic
    {
        auditStorage.put("recipe-request-received", new ArrayList<>());
        auditStorage.put("recipe-request-approved", new ArrayList<>());
        auditStorage.put("recipe-request-rejected", new ArrayList<>());
    }

    // POST endpoint for recipe-request-received
    @POST
    @Path("/topic/recipe-request-received")
    public Response submitReceivedRequest(AuditMessage message) {
        return submitAuditMessage("recipe-request-received", message);
    }

    // GET endpoint for recipe-request-received
    @GET
    @Path("/topic/recipe-request-received")
    public Response getReceivedRequests() {
        return getAuditMessages("recipe-request-received");
    }

    // POST endpoint for recipe-request-approved
    @POST
    @Path("/topic/recipe-request-approved")
    public Response submitApprovedRequest(AuditMessage message) {
        return submitAuditMessage("recipe-request-approved", message);
    }

    // GET endpoint for recipe-request-approved
    @GET
    @Path("/topic/recipe-request-approved")
    public Response getApprovedRequests() {
        return getAuditMessages("recipe-request-approved");
    }

    // POST endpoint for recipe-request-rejected
    @POST
    @Path("/topic/recipe-request-rejected")
    public Response submitRejectedRequest(AuditMessage message) {
        return submitAuditMessage("recipe-request-rejected", message);
    }

    // GET endpoint for recipe-request-rejected
    @GET
    @Path("/topic/recipe-request-rejected")
    public Response getRejectedRequests() {
        return getAuditMessages("recipe-request-rejected");
    }

    // DELETE endpoint for recipe-request-received
    @DELETE
    @Path("/topic/recipe-request-received")
    public Response deleteReceivedRequests() {
        return deleteAuditMessages("recipe-request-received");
    }

    // DELETE endpoint for recipe-request-approved
    @DELETE
    @Path("/topic/recipe-request-approved")
    public Response deleteApprovedRequests() {
        return deleteAuditMessages("recipe-request-approved");
    }

    // DELETE endpoint for recipe-request-rejected
    @DELETE
    @Path("/topic/recipe-request-rejected")
    public Response deleteRejectedRequests() {
        return deleteAuditMessages("recipe-request-rejected");
    }

    // DELETE endpoint for all audit information
    @DELETE
    @Path("/recipe-requests/all")
    public Response deleteAllAuditMessages() {
        int totalDeleted = 0;
        for (String topic : auditStorage.keySet()) {
            List<AuditMessage> messages = auditStorage.get(topic);
            synchronized (messages) {
                totalDeleted += messages.size();
                messages.clear();
            }
        }

        return Response.ok()
                .entity(new DeleteResponse("All audit messages deleted successfully", totalDeleted))
                .build();
    }

    // Helper method to submit audit message
    private Response submitAuditMessage(String topic, AuditMessage message) {
        if (message == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Message cannot be null"))
                    .build();
        }

        if (message.getId() == null || message.getTraceId() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Both id and traceId are required"))
                    .build();
        }

        // Add timestamp if not provided
        if (message.getTimestamp() == null) {
            message.setTimestamp(System.currentTimeMillis());
        }

        List<AuditMessage> messages = auditStorage.get(topic);
        synchronized (messages) {
            messages.add(message);
        }

        return Response.status(Response.Status.CREATED)
                .entity(new SuccessResponse("Message submitted successfully", message.getId()))
                .build();
    }

    // Helper method to get audit messages
    private Response getAuditMessages(String topic) {
        List<AuditMessage> messages = auditStorage.get(topic);
        synchronized (messages) {
            return Response.ok(new ArrayList<>(messages)).build();
        }
    }

    // Helper method to delete audit messages
    private Response deleteAuditMessages(String topic) {
        List<AuditMessage> messages = auditStorage.get(topic);
        int deletedCount;
        synchronized (messages) {
            deletedCount = messages.size();
            messages.clear();
        }

        return Response.ok()
                .entity(new DeleteResponse("Messages deleted successfully from topic: " + topic, deletedCount))
                .build();
    }

    // Audit Message class
    public static class AuditMessage {
        private String id;
        private String traceId;
        private String initialRequest;
        private Long timestamp;

        public AuditMessage() {}

        public AuditMessage(String id, String traceId, String initialRequest) {
            this.id = id;
            this.traceId = traceId;
            this.initialRequest = initialRequest;
            this.timestamp = System.currentTimeMillis();
        }

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTraceId() {
            return traceId;
        }

        public void setTraceId(String traceId) {
            this.traceId = traceId;
        }

        public String getInitialRequest() {
            return initialRequest;
        }

        public void setInitialRequest(String initialRequest) {
            this.initialRequest = initialRequest;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }
    }

    // Success Response class
    public static class SuccessResponse {
        private String message;
        private String messageId;

        public SuccessResponse(String message, String messageId) {
            this.message = message;
            this.messageId = messageId;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getMessageId() {
            return messageId;
        }

        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }
    }

    // Error Response class
    public static class ErrorResponse {
        private String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }

    // Delete Response class
    public static class DeleteResponse {
        private String message;
        private int deletedCount;

        public DeleteResponse(String message, int deletedCount) {
            this.message = message;
            this.deletedCount = deletedCount;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getDeletedCount() {
            return deletedCount;
        }

        public void setDeletedCount(int deletedCount) {
            this.deletedCount = deletedCount;
        }
    }
}