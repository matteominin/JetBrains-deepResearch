package com.jetbrains.websearch.service;

import com.jetbrains.websearch.model.Query;
import com.jetbrains.websearch.model.SearchResult;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class WebSearchService {
    private final ChatClient chatClient;
    private final QueryExpansionService queryAdjustService;
    private final WebScraperService webScraperService;

    public WebSearchService(
            ChatClient.Builder clientBuilder,
            QueryExpansionService queryAdjustService,
            WebScraperService webScraperService)
    {
        this.chatClient = clientBuilder.build();
        this.queryAdjustService = queryAdjustService;
        this.webScraperService = webScraperService;
    }

    public String search(String query) throws IOException {
        Query queries = queryAdjustService.generateQueries(query);

        List<SearchResult> results = new ArrayList<>();
        for (String q : queries.queries()) {
            results.addAll(webScraperService.getResults(q));
        }

        // TODO: improve deduplication logic
        results = results.stream()
                .distinct()
                .limit(10)
                .toList();

        for(SearchResult res: results) {
            String content = webScraperService.getPageContent(res.getLink());
            res.setContent(content);
        }

        return synthesizePages(query, results);
    }

    private String synthesizePages(String originalQuery, List<SearchResult> results) {
        String systemMessage = """
                You are a research assistant that answers questions using web sources.

                Rules:
                - Use ONLY information from the provided sources
                - Synthesize information from multiple sources into a coherent answer
                - Cite sources using [1], [2], [3] etc. after each fact
                - If sources have conflicting information, present both perspectives
                - If the answer is not in the sources, clearly state "I don't have enough information"
                - Be comprehensive but concise
                - Structure your answer with clear paragraphs

                Format:
                [Your answer with citations]

                Sources:
                [List all sources with their numbers and URLs]
                """;

        StringBuilder sourcesText = new StringBuilder();
        for (int i = 0; i < results.size(); i++) {
            String index = "[" + (i + 1) + "] ";
            sourcesText.append(index)
                    .append(results.get(i).toString());
        }

        String userMessage = String.format("""
                Question: %s

                Available Sources:
                %s

                Provide a comprehensive answer with citations.
                """,
                originalQuery,
                sourcesText
        );

        return chatClient.prompt()
                .system(systemMessage)
                .user(userMessage)
                .call()
                .content();
    }
}
