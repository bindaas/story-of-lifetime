package com.rajivnarula.storyoflifetime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller — receives story generation requests from the browser
 * and returns the generated story as JSON.
 */
@RestController
@RequestMapping("/api")
public class StoryController {

    private final AppConfig defaultConfig;

    public StoryController() throws Exception {
        this.defaultConfig = new AppConfig();
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generate(@RequestBody StoryRequest request) {
        try {
            // Parse facts from textarea — one per line, skip blanks
            List<String> facts = Arrays.stream(request.getFacts().split("\n"))
                    .map(String::trim)
                    .filter(line -> !line.isBlank())
                    .collect(Collectors.toList());

            // Build world model from request
            WorldModel worldModel = new WorldModel(
                    request.getStartState().trim(),
                    request.getEndState().trim(),
                    facts
            );

            // Override config with values from the form
            AppConfig config = new AppConfig(
                    request.getModel(),
                    request.getTemperature(),
                    request.getStoryLength()
            );

            // Generate story
            WriterAgent writer = new WriterAgent(config);
            String story = writer.write(worldModel);

            return ResponseEntity.ok(Map.of(
                    "story", story,
                    "factCount", String.valueOf(facts.size()),
                    "model", config.getModel(),
                    "storyLength", config.getStoryLength()
            ));

        } catch (Exception e) {
            return ResponseEntity
                    .internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
