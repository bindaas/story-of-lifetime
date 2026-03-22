package com.rajivnarula.storyoflifetime;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * WorldModel holds the current state of the story universe.
 * It reads start state, end state, and facts from the facts/ directory.
 * This is the single source of truth passed into every agent.
 */
public class WorldModel {

    private final String startState;
    private final String endState;
    private final List<String> facts;

    private static final String FACTS_DIR = "facts";

    public WorldModel() throws IOException {
        this.startState = readFile(FACTS_DIR + "/start.txt");
        this.endState   = readFile(FACTS_DIR + "/end.txt");
        this.facts      = readFacts(FACTS_DIR + "/facts.txt");
    }

    // -- Accessors --

    public String getStartState() {
        return startState;
    }

    public String getEndState() {
        return endState;
    }

    public List<String> getFacts() {
        return facts;
    }

    public int getFactCount() {
        return facts.size();
    }

    // -- Display --

    public void print() {
        System.out.println("Start  : " + startState);
        System.out.println("End    : " + endState);
        System.out.println("Facts  : " + facts.size());
        for (int i = 0; i < facts.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + facts.get(i));
        }
    }

    // -- Private helpers --

    private String readFile(String relativePath) throws IOException {
        Path path = Paths.get(relativePath);
        if (!Files.exists(path)) {
            throw new IOException("Required file not found: " + path.toAbsolutePath());
        }
        return Files.readString(path).trim();
    }

    private List<String> readFacts(String relativePath) throws IOException {
        Path path = Paths.get(relativePath);
        if (!Files.exists(path)) {
            throw new IOException("Required file not found: " + path.toAbsolutePath());
        }
        return Files.readAllLines(path)
                .stream()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .collect(Collectors.toList());
    }
}
