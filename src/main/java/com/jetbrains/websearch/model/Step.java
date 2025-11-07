package com.jetbrains.websearch.model;

public record Step(
        String stepQuery,
        Boolean searchNeeded
) { }