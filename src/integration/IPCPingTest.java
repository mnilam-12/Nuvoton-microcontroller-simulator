package src.integration;

import src.ipc.IPCChannel;
import src.ipc.IPCMessage;
import src.ipc.CoreProcess;
import src.cpu.CPU;
import src.log.LoggerProcess;

public class IPCPingTest {

    public static void main(String[] args) throws Exception {

        LoggerProcess logger = new LoggerProcess();
        logger.start();

        IPCChannel channel = new IPCChannel();

        CPU cpu = new CPU();

        // Start Core
        CoreProcess core = CoreProcess.startCore(
                channel,
                logger,
                cpu
        );

        System.out.println("[TEST] Core started");

        Thread.sleep(200);

        // Send PING
        channel.sendToCore(
                new IPCMessage("PING", "Testing IPC")
        );

        System.out.println("[TEST] PING sent");

        // Wait for PONG
        long deadline = System.currentTimeMillis() + 5000;

        IPCMessage response = null;

        while (System.currentTimeMillis() < deadline) {

            response = channel.receiveFromCore();

            if (response != null) {
                break;
            }

            Thread.sleep(10);
        }

        if (response != null) {

            System.out.println(
                    "[TEST] Core response: "
                    + response.getData()
            );

            if ("PONG".equals(response.getData())) {
                System.out.println(
                        "SUCCESS: UI-to-Core IPC communication works."
                );
            }

        } else {

            System.out.println(
                    "FAILED: Core did not respond to PING."
            );
        }

        // Stop Core
        channel.sendToCore(
                new IPCMessage("STOP", null)
        );

        Thread.sleep(200);

        // Stop Logger
        logger.shutdown();
        logger.awaitTermination();
    }
}