package com.rajivnarula.storyoflifetime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Orchestrates the agent pipeline: Planner → Writer.
 * Returns a StoryResponse with separate cost breakdown per agent.
 */
@RestController
@RequestMapping("/api")
public class StoryController {

    @PostMapping("/generate")
    public ResponseEntity<?> generate(@RequestBody StoryRequest request) {
        try {
            // Parse facts
            List<String> facts = Arrays.stream(request.getFacts().split("\n"))
                    .map(String::trim)
                    .filter(line -> !line.isBlank())
                    .collect(Collectors.toList());

            WorldModel worldModel = new WorldModel(
                    request.getStartState().trim(),
                    request.getEndState().trim(),
                    facts
            );

            AppConfig config = new AppConfig(
                    request.getPlannerModel(),
                    request.getPlannerTemperature(),
                    request.getWriterModel(),
                    request.getWriterTemperature(),
                    request.getStoryLength()
            );

            // Step 1 — Planner
            PlannerAgent  planner      = new PlannerAgent(config);
            PlannerResult plannerResult = planner.plan(worldModel);

            // Step 2 — Writer (receives Planner outline)
            WriterAgent  writer      = new WriterAgent(config);
            WriterResult writerResult = writer.write(worldModel, plannerResult.getOutline());

            StoryResponse response = new StoryResponse(
                    plannerResult.getOutline(),
                    writerResult.getStory(),
                    facts.size(),
                    config.getStoryLengthRaw(),
                    config.getPlannerModel(),
                    plannerResult.getInputTokens(),
                    plannerResult.getOutputTokens(),
                    plannerResult.getCostUsd(),
                    plannerResult.getElapsedMs(),
                    config.getWriterModel(),
                    writerResult.getInputTokens(),
                    writerResult.getOutputTokens(),
                    writerResult.getCostUsd(),
                    writerResult.getElapsedMs()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity
                    .internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
