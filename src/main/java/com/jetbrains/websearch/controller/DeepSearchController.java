package com.jetbrains.websearch.controller;

import com.jetbrains.websearch.service.DeepSearchService;
import com.jetbrains.websearch.service.WebSearchService;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/hire-me/deep-search")
public class DeepSearchController {

    private final DeepSearchService deepSearchService;

    public DeepSearchController(DeepSearchService deepSearchService) {
        this.deepSearchService = deepSearchService;
    }

    @GetMapping
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<String> search(@RequestBody String query) throws IOException {
        String response = deepSearchService.search(query);
        return ResponseEntity.ok(response);
    }
}
