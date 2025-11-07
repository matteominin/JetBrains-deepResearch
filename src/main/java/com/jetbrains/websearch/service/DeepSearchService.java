package com.jetbrains.websearch.service;

import com.jetbrains.websearch.model.Plan;
import com.jetbrains.websearch.model.Step;
import com.jetbrains.websearch.model.StepResult;
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

        String accumulatedResearch = "";
        int stepCount = 0;
        int maxSteps = 15; // Safety limit to prevent infinite loops

        for (int i = 0; i < plan.planSteps().size() && stepCount < maxSteps; i++) {
            Step currentStep = plan.planSteps().get(i);
            stepCount++;

            log.info("Executing step {}: {}", stepCount, currentStep.stepQuery());

            String webSearchResult = "";
            if(currentStep.searchNeeded()) {
                webSearchResult = webSearchService.search(currentStep.stepQuery());
            }

            StepResult result = executeStep(currentStep, accumulatedResearch, webSearchResult);
            accumulatedResearch += "\n\n--- Step " + stepCount + " ---\n" + result.synthesis();

            if (result.newSteps() != null && !result.newSteps().isEmpty()) {
                log.info("Discovered {} new research steps", result.newSteps().size());
                plan.planSteps().addAll(i + 1, result.newSteps());
            }
        }

        return accumulatedResearch;
    }

    private StepResult executeStep(Step step, String accumulatedResearch, String webSearchResult) throws IOException {
        log.info("Step: {} Search: {}", step.stepQuery(), step.searchNeeded());
        String systemPrompt = """
                You are a deep research analyst executing a specific research step as part of a comprehensive investigation.

                Your goal is to:
                1. Understand the current step's objective
                2. Review all accumulated research findings
                3. Analyze new search results (if web search is performed)
                4. Synthesize information to answer the step's specific query
                5. Build a coherent narrative that connects to previous findings
                6. Identify if NEW research steps are needed based on what you discover

                Rules:
                - Focus on the current step's specific question
                - Connect and reference previous step findings when relevant
                - If new search results are provided, cite sources using [1], [2], etc.
                - Be concise but comprehensive (3-5 paragraphs)
                - Extract key facts, insights, and evidence
                - Note any contradictions or gaps in information
                - Maintain analytical objectivity

                IMPORTANT - New Steps Discovery:
                - Only suggest new steps if you discover critical information gaps or unexpected findings
                - New steps should be highly specific and directly address the gap
                - Limit to 1-2 new steps maximum
                - Do NOT create new steps just to expand research - only when truly necessary
                - If current findings are sufficient, leave newSteps empty

                Output JSON format:
                {
                  "synthesis": "your detailed synthesis with citations",
                  "newSteps": [
                    {
                      "stepQuery": "specific query for the gap discovered",
                      "searchNeeded": true/false
                    }
                  ]
                }

                If no new steps are needed, use: "newSteps": []
                """;

        String userMessage;

        if (step.searchNeeded()) {
            String searchResults = webSearchService.search(step.stepQuery());

            userMessage = String.format("""
                    CURRENT STEP QUERY: %s

                    ACCUMULATED RESEARCH:
                    %s

                    NEW SEARCH RESULTS:
                    %s

                    Synthesize the search results and identify any critical gaps requiring new research steps.
                    """,
                    step.stepQuery(),
                    accumulatedResearch.isEmpty() ? "This is the first step - no previous findings." : accumulatedResearch,
                    searchResults
            );
        } else {
            userMessage = String.format("""
                    CURRENT STEP (SYNTHESIS/ANALYSIS): %s

                    ACCUMULATED RESEARCH:
                    %s

                    Analyze and synthesize all accumulated findings. Identify any critical gaps requiring additional research.
                    """,
                    step.stepQuery(),
                    accumulatedResearch.isEmpty() ? "No previous findings available." : accumulatedResearch
            );
        }

        return chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .call()
                .entity(StepResult.class);
    }
}
