package com.rajivnarula.storyoflifetime;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * AppConfig loads system.properties from the classpath.
 * All agents read their settings from here — no hardcoded values.
 */
public class AppConfig {

    private final Properties props = new Properties();

    private static final String PROPS_FILE = "system.properties";

    public AppConfig() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(PROPS_FILE)) {
            if (is == null) {
                throw new IOException("system.properties not found on classpath");
            }
            props.load(is);
        }
    }

    public String getModel() {
        return props.getProperty("claude.model", "claude-sonnet-4-6");
    }

    public int getMaxTokens() {
        return Integer.parseInt(props.getProperty("claude.max_tokens", "2048"));
    }

    public double getWriterTemperature() {
        return Double.parseDouble(props.getProperty("writer.temperature", "0.8"));
    }

    public String getStoryLength() {
        return switch (props.getProperty("writer.story_length", "medium").toLowerCase()) {
            case "short"  -> "2 to 3 paragraphs";
            case "long"   -> "8 to 10 paragraphs";
            default       -> "4 to 6 paragraphs";
        };
    }
}
