package com.rajivnarula.storyoflifetime;

public class Main {

    public static void main(String[] args) throws Exception {

        System.out.println("=== Story of a Lifetime ===");
        System.out.println("Phase 3: World Model — reading from config files\n");

        // Load config and world model from files
        AppConfig  config     = new AppConfig();
        WorldModel worldModel = new WorldModel();

        System.out.println("Model       : " + config.getModel());
        System.out.println("Temperature : " + config.getWriterTemperature());
        System.out.println("Story length: " + config.getStoryLength());
        System.out.println();

        worldModel.print();

        System.out.println("\nGenerating story...\n");
        System.out.println("─".repeat(60));

        WriterAgent writer = new WriterAgent(config);
        String story = writer.write(worldModel);

        System.out.println(story);
        System.out.println("─".repeat(60));
        System.out.println("\nPhase 3 complete.");
    }
}
