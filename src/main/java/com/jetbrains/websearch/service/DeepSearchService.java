package com.jetbrains.websearch.service;

import com.jetbrains.websearch.model.Plan;
import com.jetbrains.websearch.model.Step;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class DeepSearchService {

    public final ChatClient chatClient;
    private final SearchPlannerService searchPlannerService;
    private final WebSearchService webSearchService;

    public DeepSearchService(
            ChatClient.Builder chatClientBuilder,
            SearchPlannerService searchPlannerService,
            WebSearchService webSearchService
    ) {
        this.chatClient = chatClientBuilder.build();
        this.searchPlannerService = searchPlannerService;
        this.webSearchService = webSearchService;
    }

    public String search(String query) throws IOException {
        Plan plan = searchPlannerService.makePlan(query);

        String lastStepResult = "";
        for (Step s: plan.planSteps()) {
            String webSearchResult = "";
            if(s.searchNeeded()) {
                webSearchResult = webSearchService.search(s.stepQuery());
            }
            lastStepResult = executeStep(s, lastStepResult, webSearchResult);
        }

        return lastStepResult;
    }

    private String executeStep(Step step, String previousStepResult, String webSearchResult) throws IOException {
        log.info("Step: {} Search: {}", step.stepQuery(), step.searchNeeded());
        String systemPrompt = """
                You are a deep research analyst executing a specific research step as part of a comprehensive investigation.

                Your goal is to:
                1. Understand the current step's objective
                2. Review findings from the previous step (if available)
                3. Analyze new search results (if web search is needed)
                4. Synthesize information to answer the step's specific query
                5. Build a coherent narrative that connects to previous findings

                Rules:
                - Focus on the current step's specific question
                - Connect and reference previous step findings when relevant
                - If new search results are provided, cite sources using [1], [2], etc.
                - Be concise but comprehensive (3-5 paragraphs)
                - Extract key facts, insights, and evidence
                - Note any contradictions or gaps in information
                - Maintain analytical objectivity

                Output format:
                A structured synthesis containing:
                - Direct answer to the step's query
                - Key findings and insights (2-4 bullet points)
                - Connections to previous research
                - Source citations when applicable
                """;

        String userMessage;

        if (step.searchNeeded()) {
            // Perform web search and include results
            String searchResults = webSearchService.search(step.stepQuery());

            userMessage = String.format("""
                    CURRENT STEP QUERY: %s

                    PREVIOUS STEP FINDINGS:
                    %s

                    NEW SEARCH RESULTS:
                    %s

                    Please synthesize the search results for this step, building upon previous findings.
                    """,
                    step.stepQuery(),
                    previousStepResult.isEmpty() ? "This is the first step - no previous findings." : previousStepResult,
                    searchResults
            );
        } else {
            // Analysis/synthesis step without new search
            userMessage = String.format("""
                    CURRENT STEP (SYNTHESIS/ANALYSIS): %s

                    ALL PREVIOUS FINDINGS:
                    %s

                    Please analyze and synthesize the accumulated research findings.
                    """,
                    step.stepQuery(),
                    previousStepResult.isEmpty() ? "No previous findings available." : previousStepResult
            );
        }

        return chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .call()
                .content();
    }
}
