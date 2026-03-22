package com.rajivnarula.storyoflifetime;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * AppConfig loads defaults from system.properties.
 * Can be overridden per-request with values from the browser form.
 */
public class AppConfig {

    private final String model;
    private final double temperature;
    private final String storyLength;
    private final int maxTokens;

    private static final String PROPS_FILE = "system.properties";

    /** Default constructor — reads from system.properties */
    public AppConfig() throws IOException {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(PROPS_FILE)) {
            if (is == null) {
                throw new IOException("system.properties not found on classpath");
            }
            props.load(is);
        }
        this.model       = props.getProperty("claude.model",        "claude-sonnet-4-6");
        this.temperature = Double.parseDouble(props.getProperty("writer.temperature", "0.8"));
        this.storyLength = props.getProperty("writer.story_length", "medium");
        this.maxTokens   = Integer.parseInt(props.getProperty("claude.max_tokens",    "2048"));
    }

    /** Per-request constructor — values come from the browser form */
    public AppConfig(String model, double temperature, String storyLength) throws IOException {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(PROPS_FILE)) {
            if (is != null) props.load(is);
        }
        this.model       = model;
        this.temperature = temperature;
        this.storyLength = storyLength;
        this.maxTokens   = Integer.parseInt(props.getProperty("claude.max_tokens", "2048"));
    }

    public String getModel()       { return model; }
    public double getTemperature() { return temperature; }
    public int    getMaxTokens()   { return maxTokens; }

    public String getStoryLength() {
        return switch (storyLength.toLowerCase()) {
            case "short" -> "2 to 3 paragraphs";
            case "long"  -> "8 to 10 paragraphs";
            default      -> "4 to 6 paragraphs";
        };
    }
}
