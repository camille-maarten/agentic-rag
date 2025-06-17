package org.acme.agents;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.chat.SessionScopedChatBot;
import org.acme.utils.DataMessage;

@Path("/api/agent")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class RecipeValidatorAgent {

    private final SessionScopedChatBot bot;

    public RecipeValidatorAgent(SessionScopedChatBot bot) {
        this.bot = bot;
    }

    @POST
    @Path("/recipe-request-validator")
    public Response submitReceivedRequest(String message) throws Exception {
        try {
            System.out.println("/topic/recipe-request-validator => " + message);
            var dataMessage = new ObjectMapper().readValue(message, DataMessage.class);
            String itARecipeResult = bot.isItARecipe(dataMessage.getContent());
            System.out.println("is it a recipe? " + itARecipeResult);
        } catch (Exception e) {
            System.out.println("error: " + this.getClass().getSimpleName() + ": " + e.getMessage());
        }
        return Response.ok().build();
    }
}
