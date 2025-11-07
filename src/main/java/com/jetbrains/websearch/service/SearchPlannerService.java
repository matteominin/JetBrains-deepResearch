package com.jetbrains.websearch.service;

import com.jetbrains.websearch.model.Plan;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class SearchPlannerService {

    private final ChatClient chatClient;

    public SearchPlannerService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public Plan makePlan(String query) {
        String systemPrompt = """
                You are a research planning expert that breaks down complex queries into systematic research steps.

                Your goal is to create a comprehensive research plan that explores a topic deeply and methodically.

                Rules:
                - Analyze the user's query to understand the core research question
                - Break down the research into 5-8 logical steps that build upon each other
                - Each step should have a clear, specific search query
                - Steps should progress from foundational knowledge to advanced/specific insights
                - Mark searchNeeded=true for steps requiring web search
                - Mark searchNeeded=false for steps that synthesize or analyze previous findings
                - Consider multiple perspectives and authoritative sources
                - Include steps that validate and cross-reference information

                Step progression strategy:
                1. Start with foundational/definitional queries
                2. Explore current state and recent developments
                3. Investigate specific aspects or use cases
                4. Examine expert opinions and best practices
                5. Compare alternatives or competing viewpoints
                6. Verify with authoritative sources
                7. Synthesize findings (searchNeeded=false)

                Example:
                User: "How does quantum computing work and what are its real-world applications?"
                Output: {
                  "originalQuery": "quantum computing fundamentals and applications",
                  "planSteps": [
                    {
                      "stepQuery": "quantum computing basics principles fundamentals",
                      "searchNeeded": true
                    },
                    {
                      "stepQuery": "quantum computing vs classical computing differences",
                      "searchNeeded": true
                    },
                    {
                      "stepQuery": "quantum computing real-world applications 2024",
                      "searchNeeded": true
                    },
                    {
                      "stepQuery": "quantum computing industry use cases IBM Google",
                      "searchNeeded": true
                    },
                    {
                      "stepQuery": "quantum computing limitations challenges",
                      "searchNeeded": true
                    },
                    {
                      "stepQuery": "analyze and synthesize quantum computing findings",
                      "searchNeeded": false
                    }
                  ]
                }
                """;

        return chatClient.prompt()
                .system(systemPrompt)
                .user(query)
                .call()
                .entity(Plan.class);
    }
}
