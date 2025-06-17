package org.acme.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.utils.DataMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Path("/api/audit")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class RecipeRequestsAuditResource {

    // In-memory storage for audit messages by topic
    private final ConcurrentMap<String, List<DataMessage>> auditStorage = new ConcurrentHashMap<>();

    // Initialize storage for each topic
    {
        auditStorage.put("recipe-request-received", new ArrayList<>());
        auditStorage.put("recipe-request-approved", new ArrayList<>());
        auditStorage.put("recipe-request-rejected", new ArrayList<>());
    }

    // POST endpoint for recipe-request-received
    @POST
    @Path("/topic/recipe-request-received")
    public Response submitReceivedRequest(String message) throws Exception {
        try {
            System.out.println("/topic/recipe-request-received => " + message);
            var dataMessage = new ObjectMapper().readValue(message, DataMessage.class);
            return submitDataMessage("recipe-request-received", dataMessage);
        } catch (Exception e) {
            System.out.println("error: " + e.getMessage());
            return Response.ok().build();
        }
    }

    // GET endpoint for recipe-request-received
    @GET
    @Path("/topic/recipe-request-received")
    public Response getReceivedRequests() {
        return getDataMessages("recipe-request-received");
    }

    // POST endpoint for recipe-request-approved
    @POST
    @Path("/topic/recipe-request-approved")
    public Response submitApprovedRequest(String message) {
        try {
            System.out.println("/topic/recipe-request-approved => " + message);
            var dataMessage = new ObjectMapper().readValue(message, DataMessage.class);
            return submitDataMessage("recipe-request-approved", dataMessage);
        } catch (Exception e) {
            System.out.println("error: " + e.getMessage());
            return Response.ok().build();
        }
    }

    // GET endpoint for recipe-request-approved
    @GET
    @Path("/topic/recipe-request-approved")
    public Response getApprovedRequests() {
        return getDataMessages("recipe-request-approved");
    }

    // POST endpoint for recipe-request-rejected
    @POST
    @Path("/topic/recipe-request-rejected")
    public Response submitRejectedRequest(String message) {
        try {
            System.out.println("/topic/recipe-request-rejected => " + message);
            var dataMessage = new ObjectMapper().readValue(message, DataMessage.class);
            return submitDataMessage("recipe-request-rejected", dataMessage);
        } catch (Exception e) {
            System.out.println("error: " + e.getMessage());
            return Response.ok().build();
        }
    }

    // GET endpoint for recipe-request-rejected
    @GET
    @Path("/topic/recipe-request-rejected")
    public Response getRejectedRequests() {
        return getDataMessages("recipe-request-rejected");
    }

    // DELETE endpoint for recipe-request-received
    @DELETE
    @Path("/topic/recipe-request-received")
    public Response deleteReceivedRequests() {
        return deleteDataMessages("recipe-request-received");
    }

    // DELETE endpoint for recipe-request-approved
    @DELETE
    @Path("/topic/recipe-request-approved")
    public Response deleteApprovedRequests() {
        return deleteDataMessages("recipe-request-approved");
    }

    // DELETE endpoint for recipe-request-rejected
    @DELETE
    @Path("/topic/recipe-request-rejected")
    public Response deleteRejectedRequests() {
        return deleteDataMessages("recipe-request-rejected");
    }

    // DELETE endpoint for all audit information
    @DELETE
    @Path("/recipe-requests/all")
    public Response deleteAllDataMessages() {
        int totalDeleted = 0;
        for (String topic : auditStorage.keySet()) {
            List<DataMessage> messages = auditStorage.get(topic);
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
    private Response submitDataMessage(String topic, DataMessage message) {
        if (message == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Message cannot be null"))
                    .build();
        }

        if (message.getOriginalRequest() == null || message.getMessageId() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Both originalRequest and messageId are required"))
                    .build();
        }

        List<DataMessage> messages = auditStorage.get(topic);
        synchronized (messages) {
            messages.add(message);
        }

        return Response.status(Response.Status.CREATED)
                .entity(new SuccessResponse("Message submitted successfully", message.getMessageId()))
                .build();
    }

    // Helper method to get audit messages
    private Response getDataMessages(String topic) {
        List<DataMessage> messages = auditStorage.get(topic);
        synchronized (messages) {
            return Response.ok(new ArrayList<>(messages)).build();
        }
    }

    // Helper method to delete audit messages
    private Response deleteDataMessages(String topic) {
        List<DataMessage> messages = auditStorage.get(topic);
        int deletedCount;
        synchronized (messages) {
            deletedCount = messages.size();
            messages.clear();
        }

        return Response.ok()
                .entity(new DeleteResponse("Messages deleted successfully from topic: " + topic, deletedCount))
                .build();
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