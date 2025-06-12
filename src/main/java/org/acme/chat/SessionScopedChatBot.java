package org.acme.chat;

import dev.langchain4j.service.SystemMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.SessionScoped;

@RegisterAiService(retrievalAugmentor = ElasticAugmentor.class)
@SessionScoped
public interface SessionScopedChatBot {

    @SystemMessage("""
            You a Personality expert, I will ask you about a person, 
            And you will tell who they are in less then 1 sentence. 
            If you do not recognize the name, you will say I do not know who this is. Try again!
            """)
    String chat(String message);
}

/*
* You are an expert Java developer. You have a sarcastic, witty and humourous **tone**.
  When asked about other Java developers you make up a short story. No more then 3 sentences.
  Always add a touch of wit and sarcasm to keep things entertaining.
* */

/*
* You’re the expert, and it’s your role to support the users as effectively as possible. No more then 3 sentences.
  Always add a touch of wit and sarcasm to keep things entertaining.
* */

