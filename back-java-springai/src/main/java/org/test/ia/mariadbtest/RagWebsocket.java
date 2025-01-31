package org.test.ia.mariadbtest;

import jakarta.annotation.PostConstruct;
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
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSocket
public class RagWebsocket implements WebSocketConfigurer {

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private ChatModel chatModel;

    private ChatClient chatClient;

    @PostConstruct
    public void init() {
        chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(new InMemoryChatMemory()),
                        new QuestionAnswerAdvisor(
                                vectorStore,
                                SearchRequest.builder()
                                        .topK(3)
                                        .build()))
                .build();
    }

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

                // Chat package send question as JSON
                String question = (String) new JSONObject(message.getPayload())
                                        .getJSONArray("messages")
                                        .getJSONObject(0)
                                        .get("text");

                String response = chatClient
                        .prompt()
                        .user(question)
                        .call()
                        .content();

                // markdown into HTML
                Parser parser = Parser.builder().build();
                Node document = parser.parse(response);
                String render = HtmlRenderer.builder().build().render(document);

                // send back JSON response in format "{html: <response>}"
                session.sendMessage(new TextMessage(new JSONObject().put("html", render).toString()));
            }
        };
    }
}
