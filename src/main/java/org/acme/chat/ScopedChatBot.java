package org.acme.chat;

import dev.langchain4j.service.SystemMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.rag.ElasticAugmentor;

@RegisterAiService(retrievalAugmentor = ElasticAugmentor.class)
@ApplicationScoped
public interface ScopedChatBot {

    @SystemMessage("""
            Help by describing recipes a user is asking for.
            ---
            User input:  
            {message}

            ---
            Answer based on the provided context. Context: {it}
            """)
    String chat(String message);

    @SystemMessage("""
            Create a magic spell that could come from a Harry Potter movie.
                        
            Introduce what it does and how it can make fun of your waiting time for a response, max 2 sentences.

            It should be related to the user request: {message}
                        
            """)
    String magicSpell(String message);

    @SystemMessage("""
            Validate if the given user input concerns a recipe, if not, only and only return false, if so, and only if so, return true.
            It is important that you don't return anything else than true or false.
                        
            ---
            user input: {message}
            """)
    String isItARecipe(String message);

    @SystemMessage("""
            You are a dietary validation assistant. Your task is to determine whether a requested recipe is likely to **comply with the user’s dietary restrictions**.
                        
            Only return:
            - `true` if the recipe clearly does **not** contain any of the restricted ingredients.
            - `false` if the recipe **does** or **likely might** contain any of the restricted ingredients.
                    
            Avoid being overly strict: if an ingredient is extremely unlikely to appear in a recipe (e.g., pistachio in spaghetti), it can be considered safe and **should not** cause a `false`.
                    
            **Do not return any explanation — only return `true` or `false`.**
                    
            ---
                    
            {message}
            """)
    String doesTheRecipeMatchDietaryRestrictions(String message);
}

/*
*
Help by describing recipes a user is asking for.
---
User input:
{message}

---
Answer based on the provided context. Context: {it}
* */

/*
*
You a Personality expert, I will ask you about a person,
And you will tell who they are in less then 1 sentence.
If you do not recognize the name, you will say I do not know who this is. Try again!
* */

/*
*
Given the following user input, suggest a recipe that best matches their request.
The user is required to mention at least two ingredients, and their request may include dietary preferences, dish types, or cuisines.

- Focus on relevance to the mentioned ingredients.
- If the input is vague or fewer than 2 ingredients are provided, ask the user to clarify.
- Respond with a recipe name, short description, and ingredient list. Optionally include cooking time or steps if clear.

---
User input:
{user_input}
---
* */

/*
* You are an expert Java developer. You have a sarcastic, witty and humourous **tone**.
  When asked about other Java developers you make up a short story. No more then 3 sentences.
  Always add a touch of wit and sarcasm to keep things entertaining.
* */

/*
* You’re the expert, and it’s your role to support the users as effectively as possible. No more then 3 sentences.
  Always add a touch of wit and sarcasm to keep things entertaining.
* */

