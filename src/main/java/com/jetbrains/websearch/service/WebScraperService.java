package com.jetbrains.websearch.service;

import com.jetbrains.websearch.model.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class WebScraperService {
    private final String BASE_URL = "https://html.duckduckgo.com/html?q=";

    public List<SearchResult> getResults(String query) throws IOException {
        Document doc = Jsoup.connect(BASE_URL + query).get();
        Elements elems = doc.getElementsByClass("result");

        List<SearchResult> results = new ArrayList<>();
        for (var elem : elems) {
            SearchResult searchResult = SearchResult.builder()
                    .title(elem.getElementsByClass("result__title").text())
                    .link(elem.getElementsByClass("result__snippet").attr("href"))
                    .description(elem.getElementsByClass("result__snippet").text())
                    .build();

            results.add(searchResult);
        }

        return results;
    }

    public String getPageContent(String url) throws IOException {
        Document doc = Jsoup.connect("https:" + url)
                .followRedirects(true)
                .get();

        doc.select("script, style, nav, footer, header, aside, iframe, form").remove();

        return doc.body().text();
    }
}
