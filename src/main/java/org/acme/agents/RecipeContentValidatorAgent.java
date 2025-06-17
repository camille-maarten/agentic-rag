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
import org.acme.agents.clients.DietaryRestrictionsClient;
import org.acme.chat.ApplicationScopedChatBot;
import org.acme.utils.DataMessage;
import org.acme.utils.HttpUtils;

import java.util.List;

@Path("/api/agent")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class RecipeContentValidatorAgent {

    @Inject
    ApplicationScopedChatBot bot;

    @Inject
    DietaryRestrictionsClient client;

    @POST
    @Path("/recipe-content-validator")
    public Response submitReceivedRequest(String message) throws Exception {
        try {
            System.out.println("/agent/recipe-content-validator => " + message);
            var dataMessage = new ObjectMapper().readValue(message, DataMessage.class);

            List<String> restrictions = client.getDietaryRestrictions();
            System.out.println("Dietary restrictions: " + restrictions);

            String prompt = """
                    recipe: %s
                    restrictions: %s
                    """.formatted(dataMessage.getContent(), String.join(",","restrictions"));
            String isTheRecipeOkResult = bot.doesTheRecipeMatchDietaryRestrictions(prompt);

            System.out.println("is the recipe ok? " + isTheRecipeOkResult);
            if ("false".equalsIgnoreCase(isTheRecipeOkResult)) {
                HttpUtils.httpCall("kafka-native-broker-recipe-content-validation-rejected-data", dataMessage.getOriginalRequest(), "it got declined as being a valid recipe: " + isTheRecipeOkResult + " => " + String.join(",", restrictions));
                HttpUtils.httpCall("kafka-native-broker-broadcast-message-data", dataMessage.getOriginalRequest(), "No no no, you are not allowed to eat " + String.join(",", restrictions) + "!");
            } else {
                HttpUtils.httpCall("kafka-native-broker-recipe-content-validation-approved-data", dataMessage.getOriginalRequest(), dataMessage.getContent());
            }
        } catch (Exception e) {
            System.out.println("error: " + this.getClass().getSimpleName() + ": " + e.getMessage());
        }
        return Response.ok().build();
    }
}
