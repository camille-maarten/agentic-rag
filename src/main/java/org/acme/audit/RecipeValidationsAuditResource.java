package org.acme.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
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
public class RecipeValidationsAuditResource {

    // In-memory storage for audit messages by topic
    private final ConcurrentMap<String, List<DataMessage>> auditStorage = new ConcurrentHashMap<>();

    // Initialize storage for each topic
    {
        auditStorage.put("recipe-validation-received", new ArrayList<>());
        auditStorage.put("recipe-validation-approved", new ArrayList<>());
        auditStorage.put("recipe-validation-rejected", new ArrayList<>());
    }

    // POST endpoint for recipe-validation-received
    @POST
    @Path("/topic/recipe-validation-received")
    public Response submitReceivedRequest(String message) {
        try {
            System.out.println("/topic/recipe-validation-received => " + message);
            var dataMessage = new ObjectMapper().readValue(message, DataMessage.class);
            return submitDataMessage("recipe-validation-received", dataMessage);
        } catch (Exception e) {
            System.out.println("error: " + e.getMessage());
            return Response.ok().build();
        }
    }

    // GET endpoint for recipe-validation-received
    @GET
    @Path("/topic/recipe-validation-received")
    public Response getReceivedRequests() {
        return getDataMessages("recipe-validation-received");
    }

    // POST endpoint for recipe-validation-approved
    @POST
    @Path("/topic/recipe-validation-approved")
    public Response submitApprovedRequest(DataMessage message) {
        return submitDataMessage("recipe-validation-approved", message);
    }

    // GET endpoint for recipe-validation-approved
    @GET
    @Path("/topic/recipe-validation-approved")
    public Response getApprovedRequests() {
        return getDataMessages("recipe-validation-approved");
    }

    // POST endpoint for recipe-validation-rejected
    @POST
    @Path("/topic/recipe-validation-rejected")
    public Response submitRejectedRequest(DataMessage message) {
        return submitDataMessage("recipe-validation-rejected", message);
    }

    // GET endpoint for recipe-validation-rejected
    @GET
    @Path("/topic/recipe-validation-rejected")
    public Response getRejectedRequests() {
        return getDataMessages("recipe-validation-rejected");
    }

    // DELETE endpoint for recipe-validation-received
    @DELETE
    @Path("/topic/recipe-validation-received")
    public Response deleteReceivedRequests() {
        return deleteDataMessages("recipe-validation-received");
    }

    // DELETE endpoint for recipe-validation-approved
    @DELETE
    @Path("/topic/recipe-validation-approved")
    public Response deleteApprovedRequests() {
        return deleteDataMessages("recipe-validation-approved");
    }

    // DELETE endpoint for recipe-validation-rejected
    @DELETE
    @Path("/topic/recipe-validation-rejected")
    public Response deleteRejectedRequests() {
        return deleteDataMessages("recipe-validation-rejected");
    }

    // DELETE endpoint for all audit information
    @DELETE
    @Path("/recipe-validations/all")
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
                    .entity(new RecipeRequestsAuditResource.ErrorResponse("Message cannot be null"))
                    .build();
        }

        if (message.getOriginalRequest() == null || message.getMessageId() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new RecipeRequestsAuditResource.ErrorResponse("Both originalRequest and messageId are required"))
                    .build();
        }

        List<DataMessage> messages = auditStorage.get(topic);
        synchronized (messages) {
            messages.add(message);
        }

        return Response.status(Response.Status.CREATED)
                .entity(new RecipeRequestsAuditResource.SuccessResponse("Message submitted successfully", message.getMessageId()))
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
                .entity(new RecipeRequestsAuditResource.DeleteResponse("Messages deleted successfully from topic: " + topic, deletedCount))
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