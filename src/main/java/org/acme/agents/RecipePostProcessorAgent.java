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
import org.acme.utils.DataMessage;
import org.acme.utils.HttpUtils;

@Path("/api/agent")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class RecipePostProcessorAgent {

    @Inject
    ApplicationScopedChatBot bot;

    @POST
    @Path("/recipe-postprocessor")
    public Response submitReceivedRequest(String message) throws Exception {
        try {
            System.out.println("/agent/recipe-postprocessor => " + message);
            var dataMessage = new ObjectMapper().readValue(message, DataMessage.class);
            String itARecipeFromAValidRegionResult = bot.isItARecipeFromAValidRegion("Belgium");
            String recipe = bot.chat(dataMessage.getOriginalRequest());
            System.out.println("is it a recipe from a valid region? " + itARecipeFromAValidRegionResult);
            if ("false".equalsIgnoreCase(itARecipeFromAValidRegionResult)) {
                HttpUtils.httpCall("kafka-native-broker-recipe-postprocessor-rejected-data", dataMessage.getOriginalRequest(), "it got declined as being a recipe from a valid region: " + itARecipeFromAValidRegionResult);
                HttpUtils.httpCall("kafka-native-broker-broadcast-message-data", dataMessage.getOriginalRequest(), "No no no, you should not eat something from here!");
            } else {
                HttpUtils.httpCall("kafka-native-broker-recipe-postprocessor-approved-data", dataMessage.getOriginalRequest(), dataMessage.getContent());
                HttpUtils.httpCall("kafka-native-broker-broadcast-message-data", dataMessage.getOriginalRequest(), "Here you have your recipe, enjoy! " + recipe);
            }
        } catch (Exception e) {
            System.out.println("error: " + this.getClass().getSimpleName() + ": " + e.getMessage());
        }
        return Response.ok().build();
    }
}
