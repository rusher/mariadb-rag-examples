package org.test.ia.mariadbtest;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.*;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSocket
public class RagWebsocket implements WebSocketConfigurer {

    @Autowired
    private VectorStore vectorStore;

    private ChatMemory chatMemory = new InMemoryChatMemory();

    @Autowired
    private ChatModel chatModel;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(myHandler(), "/chatbot").setAllowedOrigins("*");
    }

    @Bean
    public WebSocketHandler myHandler() {
        return new TextWebSocketHandler() {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                session.sendMessage(new TextMessage("{\"text\": \"How can I help ?\"}"));
            }

            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
                JSONObject jo2 = new JSONObject(message.getPayload());
                JSONArray arr = jo2.getJSONArray("messages");
                JSONObject obj = arr.getJSONObject(0);
                String question = (String)obj.get("text");

                var chatClient = ChatClient.builder(chatModel)
                        .defaultAdvisors(
                                new MessageChatMemoryAdvisor(chatMemory),
                                new QuestionAnswerAdvisor(vectorStore))
                        .build();

                String response = chatClient
                        .prompt()
                        .user(question)
                        .call()
                        .content();

                // markdown into HTML
                Parser parser = Parser.builder().build();
                Node document = parser.parse(response);
                String render = HtmlRenderer.builder().build().render(document);

                JSONObject jo = new JSONObject();
                jo.put("html", render);


                session.sendMessage(new TextMessage(jo.toString()));
            }
        };
    }
}
