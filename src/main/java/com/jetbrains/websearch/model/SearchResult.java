package com.jetbrains.websearch.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchResult {
    private String title;
    private String link;
    private String description;
    private String content;

    @Override
    public String toString() {
        return  "title: " + title +
                "link: " + link +
                "description: " + description +
                "content: " + content + "\n";
    }
}
