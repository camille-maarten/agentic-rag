package org.acme.agents;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.chat.ApplicationScopedChatBot;
import org.acme.chat.SessionScopedChatBot;
import org.acme.utils.DataMessage;
import org.acme.utils.HttpUtils;

import java.util.ListIterator;

@Path("/api/agent")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class RecipeValidatorAgent {

    @Inject
    ApplicationScopedChatBot bot;

    @POST
    @Path("/recipe-request-validator")
    public Response submitReceivedRequest(String message) throws Exception {
        try {
            System.out.println("/agent/recipe-request-validator => " + message);
            var dataMessage = new ObjectMapper().readValue(message, DataMessage.class);
            String itARecipeResult = bot.isItARecipe(dataMessage.getContent());
            System.out.println("is it a recipe? " + itARecipeResult);
            if ("false".equalsIgnoreCase(itARecipeResult)) {
                HttpUtils.httpCall(
                        "kafka-native-broker-recipe-validation-rejected-data",
                        dataMessage.getOriginalRequest(),
                        "it got declined as being a recipe: " + itARecipeResult
                );
                HttpUtils.httpCall(
                        "kafka-native-broker-broadcast-message-data",
                        dataMessage.getOriginalRequest(),
                        "No no no, this is not about recipes!"
                );
            } else {
                HttpUtils.httpCall(
                        "kafka-native-broker-recipe-validation-approved-data",
                        dataMessage.getOriginalRequest(),
                        dataMessage.getContent()
                );
            }
        } catch (Exception e) {
            System.out.println("error: " + this.getClass().getSimpleName() + ": " + e.getMessage());
        }
        return Response.ok().build();
    }
}
