package com.rajivnarula.storyoflifetime.web;

import com.rajivnarula.storyoflifetime.agent.*;
import com.rajivnarula.storyoflifetime.config.AppConfig;
import com.rajivnarula.storyoflifetime.model.*;
import com.rajivnarula.storyoflifetime.result.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Orchestrates all agent pipelines:
 *   POST /api/generate-facts  — FactGenerator (human reviews before proceeding)
 *   POST /api/generate        — Planner → Critic loop → Writer
 *   POST /api/explain         — Explainer (v1 vs v2 diff)
 */
@RestController
@RequestMapping("/api")
public class StoryController {

    // ── Fact Generator ────────────────────────────────────────────────────────

    @PostMapping("/generate-facts")
    public ResponseEntity<?> generateFacts(@RequestBody FactGenerateRequest request) {
        try {
            AppConfig          config    = new AppConfig();
            FactGeneratorAgent generator = new FactGeneratorAgent(config);

            FactGeneratorResult result = generator.generate(
                    request.getStartState().trim(),
                    request.getEndState().trim(),
                    request.getCreativity(),
                    request.getContradiction(),
                    request.getWorldType(),
                    request.getFactCount()
            );

            return ResponseEntity.ok(Map.of(
                    "facts",        result.getFacts(),
                    "inputTokens",  result.getInputTokens(),
                    "outputTokens", result.getOutputTokens(),
                    "costUsd",      result.getCostUsd(),
                    "elapsedMs",    result.getElapsedMs()
            ));

        } catch (Exception e) {
            return ResponseEntity
                    .internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ── Planner → Critic → Writer ─────────────────────────────────────────────

    @PostMapping("/generate")
    public ResponseEntity<?> generate(@RequestBody StoryRequest request) {
        try {
            List<String> facts = Arrays.stream(request.getFacts().split("\n"))
                    .map(String::trim)
                    .filter(line -> !line.isBlank())
                    .collect(Collectors.toList());

            WorldModel worldModel = new WorldModel(
                    request.getStartState().trim(),
                    request.getEndState().trim(),
                    facts,
                    request.getWorldType()
            );

            AppConfig config = new AppConfig(
                    request.getPlannerModel(), request.getPlannerTemperature(),
                    request.getCriticModel(),  request.getCriticTemperature(),
                    request.getWriterModel(),  request.getWriterTemperature(),
                    request.getStoryLength()
            );

            PlannerAgent planner = new PlannerAgent(config);
            CriticAgent  critic  = new CriticAgent(config);
            WriterAgent  writer  = new WriterAgent(config);

            int    totalPlannerInput  = 0, totalPlannerOutput  = 0;
            double totalPlannerCost   = 0;
            long   totalPlannerMs     = 0;
            int    totalCriticInput   = 0, totalCriticOutput   = 0;
            double totalCriticCost    = 0;
            long   totalCriticMs      = 0;

            List<String> criticDecisions  = new ArrayList<>();
            List<String> criticReasons    = new ArrayList<>();

            String  lastOutline        = null;
            String  lastCriticFeedback = "";
            int     attempts           = 0;
            boolean approved           = false;

            while (attempts < config.getCriticMaxAttempts() && !approved) {
                attempts++;
                System.out.printf("%n[Controller] Planner attempt %d of %d%n",
                        attempts, config.getCriticMaxAttempts());

                PlannerResult plannerResult = planner.plan(worldModel, lastCriticFeedback);
                lastOutline = plannerResult.getOutline();

                totalPlannerInput  += plannerResult.getInputTokens();
                totalPlannerOutput += plannerResult.getOutputTokens();
                totalPlannerCost   += plannerResult.getCostUsd();
                totalPlannerMs     += plannerResult.getElapsedMs();

                CriticResult criticResult = critic.evaluate(worldModel, lastOutline, lastCriticFeedback);

                totalCriticInput  += criticResult.getInputTokens();
                totalCriticOutput += criticResult.getOutputTokens();
                totalCriticCost   += criticResult.getCostUsd();
                totalCriticMs     += criticResult.getElapsedMs();

                criticDecisions.add(criticResult.getDecision().name());
                criticReasons.add(criticResult.getReason());

                if (criticResult.isApproved()) {
                    approved = true;
                    System.out.println("[Controller] Outline approved — proceeding to Writer");
                } else {
                    lastCriticFeedback = criticResult.getReason();
                    System.out.println("[Controller] Outline rejected — replanning");
                }
            }

            if (!approved) {
                System.out.printf("[Controller] Max attempts (%d) reached — using last outline%n",
                        config.getCriticMaxAttempts());
            }

            WriterResult writerResult = writer.write(worldModel, lastOutline);

            StoryResponse response = new StoryResponse(
                    lastOutline,
                    writerResult.getStory(),
                    facts.size(),
                    config.getStoryLengthRaw(),
                    attempts,
                    approved,
                    criticDecisions,
                    criticReasons,
                    config.getPlannerModel(),
                    totalPlannerInput,  totalPlannerOutput,
                    totalPlannerCost,   totalPlannerMs,
                    config.getCriticModel(),
                    totalCriticInput,   totalCriticOutput,
                    totalCriticCost,    totalCriticMs,
                    config.getWriterModel(),
                    writerResult.getInputTokens(),  writerResult.getOutputTokens(),
                    writerResult.getCostUsd(),       writerResult.getElapsedMs()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity
                    .internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ── Explainer ─────────────────────────────────────────────────────────────

    @PostMapping("/explain")
    public ResponseEntity<?> explain(@RequestBody ExplainRequest request) {
        try {
            AppConfig      config    = new AppConfig();
            ExplainerAgent explainer = new ExplainerAgent(config);

            ExplainerResult result = explainer.explain(
                    request.getStartState(),
                    request.getEndState(),
                    request.getOriginalFacts(),
                    request.getAdditionalFacts(),
                    request.getStoryV1(),
                    request.getStoryV2()
            );

            return ResponseEntity.ok(Map.of(
                    "explanation",  result.getExplanation(),
                    "inputTokens",  String.valueOf(result.getInputTokens()),
                    "outputTokens", String.valueOf(result.getOutputTokens()),
                    "costUsd",      String.valueOf(result.getCostUsd()),
                    "elapsedMs",    String.valueOf(result.getElapsedMs())
            ));

        } catch (Exception e) {
            return ResponseEntity
                    .internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
