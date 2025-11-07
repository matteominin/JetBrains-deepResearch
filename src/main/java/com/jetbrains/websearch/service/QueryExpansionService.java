package com.jetbrains.websearch.service;

import com.jetbrains.websearch.model.Query;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Service
public class QueryExpansionService {

    private final ChatClient chatClient;

    public QueryExpansionService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public Query generateQueries(String query) {
        String systemMessage = """
                Generate 5 essential search queries from user input. Keep queries clean and focused.

                Rules:
                - Extract core intent, remove unnecessary words
                - Use DuckDuckGo operators ONLY when beneficial: "exact", ~"semantic", site:, -site:, filetype:
                - Prefer simple keyword queries over complex syntax
                - Each query should explore different angles
                - Target authoritative sites when relevant (stackoverflow, github, official docs)

                Output JSON:
                {
                  "userQuery": "simplified main query",
                  "queries": ["query1", "query2", "query3", "query4", "query5"]
                }

                Example:
                User: "What are the best practices for Java Spring Boot security?"
                Output: {
                  "userQuery": "Spring Boot security best practices",
                  "queries": [
                    "Spring Boot security best practices",
                    "Spring Boot authentication authorization",
                    "Spring Security configuration",
                    "Spring Boot security site:spring.io",
                    "Spring Boot security vulnerabilities"
                  ]
                }
                """;
        SystemMessage message = new SystemMessage(systemMessage);
        UserMessage userMessage = new UserMessage(query);
        Prompt prompt = new Prompt(message, userMessage);

        return chatClient.prompt(prompt)
                .call()
                .entity(Query.class);
    }
}
