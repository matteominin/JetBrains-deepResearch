package com.jetbrains.websearch.model;

import java.util.List;

public record StepResult(
        String synthesis,
        List<Step> newSteps
) { }