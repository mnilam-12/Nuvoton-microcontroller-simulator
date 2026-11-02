
package src.integration;

import src.ipc.IPCChannel;
import src.ipc.IPCMessage;
import src.ipc.CoreProcess;

import java.util.concurrent.TimeUnit;

public class Benchmark {

    public static void main(String[] args) throws Exception {

        IPCChannel channel = new IPCChannel();
        CoreProcess.startCore(channel);

        int iterations = 100;
        long totalTime = 0;
        int completed = 0;

        System.out.println("=== WEEK 4 INTEGRATION BENCHMARK ===");

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
                System.out.println("Timeout at iteration " + i);
                break;
            }
        }

        sendAndWait(channel, "STOP", null);

        if (completed == 0) {
            System.out.println("No successful measurements.");
            return;
        }

        double averageMs =
            (totalTime / (double) completed) / 1_000_000.0;

        System.out.println();
        System.out.println("Iterations: " + completed);
        System.out.printf(
            "Average command response time: %.4f ms%n",
            averageMs
        );
        System.out.println("Benchmark completed.");
    }

    private static boolean sendAndWait(
        IPCChannel channel,
        String command,
        String data
    ) throws InterruptedException {

        channel.sendToCore(new IPCMessage(command, data));

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
