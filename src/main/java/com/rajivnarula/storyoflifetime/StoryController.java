package com.rajivnarula.storyoflifetime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller — receives story generation requests from the browser
 * and returns a StoryResponse as JSON.
 */
@RestController
@RequestMapping("/api")
public class StoryController {

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
                    facts
            );

            AppConfig config = new AppConfig(
                    request.getModel(),
                    request.getTemperature(),
                    request.getStoryLength()
            );

            WriterAgent writer   = new WriterAgent(config);
            StoryResponse result = writer.write(worldModel, config.getStoryLength(), facts.size());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity
                    .internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
