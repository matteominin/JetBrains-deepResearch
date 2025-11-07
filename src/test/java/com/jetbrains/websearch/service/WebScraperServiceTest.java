package com.jetbrains.websearch.service;

import com.jetbrains.websearch.model.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

@Slf4j
@SpringBootTest
public class WebScraperServiceTest {

    @Autowired
    private WebScraperService webScraperService;

    @Test
    void testGetResults() {
        try {
            List<SearchResult> results = webScraperService.getResults("cat");
            for (SearchResult result : results) {
                log.info("Title: " + result.getTitle());
                log.info("Link: " + result.getLink());
                log.info("Description: " + result.getDescription());
            }
        } catch (IOException e) {
            log.error("Error getting results" + e.getMessage());
        }
    }
}
