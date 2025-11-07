package com.jetbrains.websearch.model;

import java.util.List;

public record Query (
        String userQuery,
        List<String> queries
) { }