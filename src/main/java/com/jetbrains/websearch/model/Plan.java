package com.jetbrains.websearch.model;

import java.util.List;

public record Plan(
        String originalQuery,
        List<Step> planSteps
) { }