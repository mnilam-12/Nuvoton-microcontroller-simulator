
package src.integration;

import src.ipc.CoreProcess;
import src.ipc.IPCChannel;
import src.ipc.IPCMessage;

public class IntegrationTest {

    private static int passed = 0;
    private static int failed = 0;

    private static void check(
            String testName, boolean condition) {

        if (condition) {
            passed++;
            System.out.println("PASS: " + testName);
        } else {
            failed++;
            System.out.println("FAIL: " + testName);
        }
    }

    private static String sendCommand(
            IPCChannel channel, String command,
            String data) throws InterruptedException {

        // Send command from UI side to Core.
        channel.sendToCore(new IPCMessage(command, data));

        // Wait for the Core response, with a timeout.
        long deadline = System.currentTimeMillis() + 3000;

        while (System.currentTimeMillis() < deadline) {
            IPCMessage response = channel.receiveFromCore();

            if (response != null) {
                return String.valueOf(response.getCommand())
                        + " " + String.valueOf(response.getData());
            }

            Thread.sleep(10);
        }

        throw new AssertionError(
                "No Core response for " + command);
    }

    public static void main(String[] args) throws Exception {

        IPCChannel channel = new IPCChannel();

        // Start Core in a background thread.
        CoreProcess.startCore(channel);

        String response;

        // Test 1: LOAD
        response = sendCommand(
                channel, "LOAD", "MOV A, #05");
        check("LOAD response",
                response.contains("Program loaded successfully"));

        // Test 2: STEP
        response = sendCommand(channel, "STEP", null);
        check("STEP returns a response",
                !response.trim().isEmpty());

        // Test 3: RESET
        response = sendCommand(channel, "RESET", null);
        check("RESET response",
                response.contains("CPU reset completed"));

        // Test 4: RUN
        response = sendCommand(
                channel, "LOAD", "MOV A, #05\nHALT");
        check("Second LOAD response",
                response.contains("Program loaded successfully"));

        
        response = sendCommand(channel, "RUN", null);

        boolean completed = response.contains("Program completed");

        // RUN can send instruction responses before completion.
        for (int i = 0; i < 110 && !completed; i++) {
            IPCMessage next = channel.receiveFromCore();

            if (next != null) {
                String result = String.valueOf(next.getCommand())
                        + " " + String.valueOf(next.getData());

                if (result.contains("Program completed")) {
                    completed = true;
                }
            } else {
                Thread.sleep(10);
            }
        }

        check("RUN reaches completion", completed);

        
        // Test 5: Unknown command
        response = sendCommand(channel, "INVALID", null);
        check("Unknown command handled",
                response.contains("Unknown command"));

        // Test 6: STOP command
        response = sendCommand(channel, "STOP", null);
        check("STOP response",
                response.contains("Core process stopped"));


        System.out.println();
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);

        if (failed > 0) {
            System.exit(1);
        }

        // Core may remain running because it has
        // a blocking receive loop. End this test JVM.
        System.exit(0);
    }
}
