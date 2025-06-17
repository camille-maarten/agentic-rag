package org.acme.chat;

import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.rag.ElasticAugmentor;

@RegisterAiService(retrievalAugmentor = ElasticAugmentor.class)
@ApplicationScoped
public interface ApplicationScopedChatBot extends ScopedChatBot {

}
