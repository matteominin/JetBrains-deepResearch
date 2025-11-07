package com.jetbrains.websearch.service;

import com.jetbrains.websearch.model.Query;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class QueryExpansionServiceTest {

    @Autowired
    private QueryExpansionService queryAdjustService;

    @Test
    public void testGenerateQueries() {
        Query query = queryAdjustService.generateQueries("Which are hackatons events are happening in Italy in 2025?");
        log.info(query.toString());
    }
}
