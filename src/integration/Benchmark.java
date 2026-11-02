package src.integration;

import src.ipc.IPCChannel;
import src.ipc.IPCMessage;
import src.ipc.CoreProcess;
import src.log.LoggerProcess;

import java.util.concurrent.TimeUnit;

public class Benchmark {

    public static void main(String[] args) throws Exception {

        // Create and start Logger
        LoggerProcess logger = new LoggerProcess();
        logger.start();

        // Create IPC communication channel
        IPCChannel channel = new IPCChannel();

        // Start Core with the shared Logger instance
        CoreProcess.startCore(channel, logger);

        int iterations = 100;
        long totalTime = 0;
        int completed = 0;

        System.out.println("=== WEEK 4 INTEGRATION BENCHMARK ===");

        try {

            // Warm up the Core thread
            sendAndWait(channel, "RESET", null);

            for (int i = 0; i < iterations; i++) {

                long start = System.nanoTime();

                boolean success = sendAndWait(
                    channel, "RESET", null
                );

                long end = System.nanoTime();

                if (success) {
                    totalTime += end - start;
                    completed++;
                } else {
                    System.out.println(
                        "Timeout at iteration " + i
                    );
                    break;
                }
            }

            if (completed == 0) {

                System.out.println(
                    "No successful measurements."
                );

            } else {

                double averageMs =
                    (totalTime / (double) completed)
                    / 1_000_000.0;

                System.out.println();
                System.out.println(
                    "Iterations: " + completed
                );

                System.out.printf(
                    "Average command response time: %.4f ms%n",
                    averageMs
                );

                System.out.println("Benchmark completed.");
            }

        } finally {

            // Stop Core process
            sendAndWait(channel, "STOP", null);

            // Stop Logger thread
            logger.shutdown();
            logger.awaitTermination();
        }
    }

    private static boolean sendAndWait(
        IPCChannel channel,
        String command,
        String data
    ) throws InterruptedException {

        // Send command to Core
        channel.sendToCore(
            new IPCMessage(command, data)
        );

        long deadline = System.nanoTime()
            + TimeUnit.SECONDS.toNanos(3);

        while (System.nanoTime() < deadline) {

            IPCMessage response = channel.receiveFromCore();

            if (response != null) {
                return true;
            }

            Thread.sleep(1);
        }

        return false;
    }
}