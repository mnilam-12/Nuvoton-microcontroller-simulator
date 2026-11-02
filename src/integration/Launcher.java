
package src.integration;

import src.Main;

public class Launcher {

    public static void main(String[] args) {

        System.out.println("=================================");
        System.out.println(" Nuvoton Simulator - Integration ");
        System.out.println("=================================");

        System.out.println("Starting simulator...");

        // Start the existing simulator.
        // Current version uses thread-based communication.
        Main.main(args);

        System.out.println("Simulator launch requested.");
    }
}
