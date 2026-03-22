package com.rajivnarula.storyoflifetime;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;

public class Main {

    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL   = "claude-opus-4-6";
    private static final String API_KEY = System.getenv("ANTHROPIC_API_KEY");

    private static final OkHttpClient httpClient = new OkHttpClient();
    private static final ObjectMapper mapper     = new ObjectMapper();

    public static void main(String[] args) throws Exception {

        System.out.println("=== Story of a Lifetime ===");
        System.out.println("Phase 1: Verifying connection to Claude...\n");

        String response = callClaude("Tell me one interesting fact about India in one sentence.");

        System.out.println("Response from Claude:");
        System.out.println(response);
        System.out.println("\nPhase 1 complete. Connection verified.");
    }

    public static String callClaude(String userMessage) throws Exception {

        if (API_KEY == null || API_KEY.isBlank()) {
            throw new RuntimeException(
                "ANTHROPIC_API_KEY environment variable is not set. " +
                "Run: export ANTHROPIC_API_KEY=your_key_here"
            );
        }

        // Build request body
        ObjectNode body = mapper.createObjectNode();
        body.put("model", MODEL);
        body.put("max_tokens", 1024);

        ArrayNode messages = mapper.createArrayNode();
        ObjectNode message = mapper.createObjectNode();
        message.put("role", "user");
        message.put("content", userMessage);
        messages.add(message);
        body.set("messages", messages);

        // Make HTTP call
        Request request = new Request.Builder()
                .url(API_URL)
                .post(RequestBody.create(
                        mapper.writeValueAsString(body),
                        MediaType.get("application/json")))
                .addHeader("x-api-key", API_KEY)
                .addHeader("anthropic-version", "2023-06-01")
                .addHeader("content-type", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body().string();

            if (!response.isSuccessful()) {
                throw new RuntimeException(
                    "API call failed [HTTP " + response.code() + "]: " + responseBody
                );
            }

            // Parse and return just the text content
            JsonNode root = mapper.readTree(responseBody);
            return root.path("content").get(0).path("text").asText();
        }
    }
}
