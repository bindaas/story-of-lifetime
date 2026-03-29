package com.rajivnarula.storyoflifetime;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * WorldModel is the single source of truth passed into every agent.
 * Supports two construction modes:
 *   - File-based: reads from the facts/ directory (CLI mode)
 *   - Direct:     accepts values from the web form (web mode)
 */
public class WorldModel {

    private final String       startState;
    private final String       endState;
    private final List<String> facts;
    private final String       worldType;

    private static final String FACTS_DIR = "facts";

    /** File-based constructor — reads from facts/ directory */
    public WorldModel() throws IOException {
        this.startState = readFile(FACTS_DIR + "/start.txt");
        this.endState   = readFile(FACTS_DIR + "/end.txt");
        this.facts      = readFacts(FACTS_DIR + "/facts.txt");
        this.worldType  = "realistic";
    }

    /** Direct constructor — values come from the web form */
    public WorldModel(String startState, String endState, List<String> facts, String worldType) {
        this.startState = startState;
        this.endState   = endState;
        this.facts      = facts;
        this.worldType  = worldType != null ? worldType : "realistic";
    }

    public String       getStartState() { return startState; }
    public String       getEndState()   { return endState; }
    public List<String> getFacts()      { return facts; }
    public int          getFactCount()  { return facts.size(); }
    public String       getWorldType()  { return worldType; }

    public void print() {
        System.out.println("Start : " + startState);
        System.out.println("End   : " + endState);
        System.out.println("Facts : " + facts.size());
        for (int i = 0; i < facts.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + facts.get(i));
        }
    }

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
        return Files.readAllLines(path).stream()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .collect(Collectors.toList());
    }
}