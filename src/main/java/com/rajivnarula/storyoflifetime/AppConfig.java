package com.rajivnarula.storyoflifetime;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * AppConfig loads defaults from system.properties.
 * Supports separate model + temperature for Planner, Critic, Writer, and Explainer.
 */
public class AppConfig {

    private final String plannerModel;
    private final double plannerTemperature;
    private final String criticModel;
    private final double criticTemperature;
    private final int    criticMaxAttempts;
    private final String writerModel;
    private final double writerTemperature;
    private final String explainerModel;
    private final double explainerTemperature;
    private final String storyLength;
    private final int    maxTokens;

    private static final String PROPS_FILE = "system.properties";

    /** Default constructor — reads from system.properties */
    public AppConfig() throws IOException {
        Properties props          = loadProps();
        this.plannerModel         = props.getProperty("planner.model",         "claude-sonnet-4-6");
        this.plannerTemperature   = Double.parseDouble(props.getProperty("planner.temperature",   "0.3"));
        this.criticModel          = props.getProperty("critic.model",          "claude-sonnet-4-6");
        this.criticTemperature    = Double.parseDouble(props.getProperty("critic.temperature",    "0.1"));
        this.criticMaxAttempts    = Integer.parseInt(props.getProperty("critic.max_attempts",     "3"));
        this.writerModel          = props.getProperty("writer.model",          "claude-opus-4-6");
        this.writerTemperature    = Double.parseDouble(props.getProperty("writer.temperature",    "0.8"));
        this.explainerModel       = props.getProperty("explainer.model",       "claude-sonnet-4-6");
        this.explainerTemperature = Double.parseDouble(props.getProperty("explainer.temperature", "0.3"));
        this.storyLength          = props.getProperty("writer.story_length",   "medium");
        this.maxTokens            = Integer.parseInt(props.getProperty("claude.max_tokens",       "2048"));
    }

    /** Per-request constructor — values come from the browser form */
    public AppConfig(String plannerModel, double plannerTemperature,
                     String criticModel,  double criticTemperature,
                     String writerModel,  double writerTemperature,
                     String storyLength) throws IOException {
        Properties props          = loadProps();
        this.plannerModel         = plannerModel;
        this.plannerTemperature   = plannerTemperature;
        this.criticModel          = criticModel;
        this.criticTemperature    = criticTemperature;
        this.criticMaxAttempts    = Integer.parseInt(props.getProperty("critic.max_attempts",     "3"));
        this.writerModel          = writerModel;
        this.writerTemperature    = writerTemperature;
        this.explainerModel       = props.getProperty("explainer.model",       "claude-sonnet-4-6");
        this.explainerTemperature = Double.parseDouble(props.getProperty("explainer.temperature", "0.3"));
        this.storyLength          = storyLength;
        this.maxTokens            = Integer.parseInt(props.getProperty("claude.max_tokens",       "2048"));
    }

    private Properties loadProps() throws IOException {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(PROPS_FILE)) {
            if (is != null) props.load(is);
        }
        return props;
    }

    public String getPlannerModel()         { return plannerModel; }
    public double getPlannerTemperature()   { return plannerTemperature; }
    public String getCriticModel()          { return criticModel; }
    public double getCriticTemperature()    { return criticTemperature; }
    public int    getCriticMaxAttempts()    { return criticMaxAttempts; }
    public String getWriterModel()          { return writerModel; }
    public double getWriterTemperature()    { return writerTemperature; }
    public String getExplainerModel()       { return explainerModel; }
    public double getExplainerTemperature() { return explainerTemperature; }
    public int    getMaxTokens()            { return maxTokens; }

    public String getStoryLength() {
        return switch (storyLength.toLowerCase()) {
            case "short" -> "2 to 3 paragraphs";
            case "long"  -> "8 to 10 paragraphs";
            default      -> "4 to 6 paragraphs";
        };
    }

    public String getStoryLengthRaw() { return storyLength; }
}
