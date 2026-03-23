package com.rajivnarula.storyoflifetime;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * AppConfig loads defaults from system.properties.
 * Supports separate model + temperature for Planner and Writer.
 * Can be overridden per-request with values from the browser form.
 */
public class AppConfig {

    private final String plannerModel;
    private final double plannerTemperature;
    private final String writerModel;
    private final double writerTemperature;
    private final String storyLength;
    private final int    maxTokens;

    private static final String PROPS_FILE = "system.properties";

    /** Default constructor — reads from system.properties */
    public AppConfig() throws IOException {
        Properties props = loadProps();
        this.plannerModel       = props.getProperty("planner.model",       "claude-sonnet-4-6");
        this.plannerTemperature = Double.parseDouble(props.getProperty("planner.temperature", "0.3"));
        this.writerModel        = props.getProperty("writer.model",        "claude-opus-4-6");
        this.writerTemperature  = Double.parseDouble(props.getProperty("writer.temperature",  "0.8"));
        this.storyLength        = props.getProperty("writer.story_length", "medium");
        this.maxTokens          = Integer.parseInt(props.getProperty("claude.max_tokens",     "2048"));
    }

    /** Per-request constructor — values come from the browser form */
    public AppConfig(String plannerModel, double plannerTemperature,
                     String writerModel,  double writerTemperature,
                     String storyLength) throws IOException {
        Properties props        = loadProps();
        this.plannerModel       = plannerModel;
        this.plannerTemperature = plannerTemperature;
        this.writerModel        = writerModel;
        this.writerTemperature  = writerTemperature;
        this.storyLength        = storyLength;
        this.maxTokens          = Integer.parseInt(props.getProperty("claude.max_tokens", "2048"));
    }

    private Properties loadProps() throws IOException {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(PROPS_FILE)) {
            if (is != null) props.load(is);
        }
        return props;
    }

    public String getPlannerModel()       { return plannerModel; }
    public double getPlannerTemperature() { return plannerTemperature; }
    public String getWriterModel()        { return writerModel; }
    public double getWriterTemperature()  { return writerTemperature; }
    public int    getMaxTokens()          { return maxTokens; }

    public String getStoryLength() {
        return switch (storyLength.toLowerCase()) {
            case "short" -> "2 to 3 paragraphs";
            case "long"  -> "8 to 10 paragraphs";
            default      -> "4 to 6 paragraphs";
        };
    }

    public String getStoryLengthRaw() { return storyLength; }
}
