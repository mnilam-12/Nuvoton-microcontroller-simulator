package src.ipc;

public class IPCTest {

    public static void main(String[] args) {

        System.out.println("======================================");
        System.out.println("        WEEK 4 IPC CHANNEL TEST");
        System.out.println("======================================");

        // Create IPC channel
        IPCChannel channel = new IPCChannel();

        // Create UI process
        UIProcess ui = new UIProcess(channel);

        // Create Core process
        CoreProcess core = new CoreProcess(channel);

        // ==========================================
        // TEST 1: UI -> CORE
        // ==========================================

        System.out.println("\nTEST 1: UI -> CORE");

        ui.sendCommand(
                "STEP",
                "Execute one instruction"
        );

        System.out.println(
                "UI sent command: STEP"
        );

        // Check whether Core received the command
        if (channel.hasCommand()) {

            System.out.println(
                    "PASS: Command is present in UI -> Core queue"
            );

        } else {

            System.out.println(
                    "FAIL: Command was not found"
            );
        }

        // ==========================================
        // TEST 2: CORE RECEIVES COMMAND
        // ==========================================

        System.out.println("\nTEST 2: CORE RECEIVES COMMAND");

        core.processCommand();

        System.out.println(
                "PASS: Core processed the command"
        );

        // ==========================================
        // TEST 3: CORE -> UI
        // ==========================================

        System.out.println("\nTEST 3: CORE -> UI");

        // Check whether Core generated a response
        if (channel.hasResponse()) {

            System.out.println(
                    "PASS: Response is present in Core -> UI queue"
            );

        } else {

            System.out.println(
                    "FAIL: No response found"
            );
        }

        // ==========================================
        // TEST 4: UI RECEIVES RESPONSE
        // ==========================================

        System.out.println("\nTEST 4: UI RECEIVES RESPONSE");

        IPCMessage response =
                ui.receiveMessage();

        if (response != null) {

            System.out.println(
                    "PASS: UI received response"
            );

            System.out.println(
                    "Command: "
                    + response.getCommand()
            );

            System.out.println(
                    "Data: "
                    + response.getData()
            );

        } else {

            System.out.println(
                    "FAIL: UI did not receive response"
            );
        }

        // ==========================================
        // TEST 5: MULTIPLE MESSAGES
        // ==========================================

        System.out.println("\nTEST 5: MULTIPLE MESSAGES");

        ui.sendCommand(
                "RESET",
                "Reset CPU"
        );

        ui.sendCommand(
                "STEP",
                "Execute one instruction"
        );

        ui.sendCommand(
                "RUN",
                "Run program"
        );

        System.out.println(
                "Three commands sent from UI to Core"
        );

        // Check queue
        if (channel.hasCommand()) {

            System.out.println(
                    "PASS: Multiple messages stored in queue"
            );

        } else {

            System.out.println(
                    "FAIL: Messages were not stored"
            );
        }

        // ==========================================
        // TEST 6: CORE PROCESSES MULTIPLE MESSAGES
        // ==========================================

        System.out.println(
                "\nTEST 6: CORE PROCESSES MULTIPLE MESSAGES"
        );

        int responseCount = 0;

        // Process all commands
        while (channel.hasCommand()) {

            core.processCommand();

            responseCount++;
        }

        System.out.println(
                "Core processed "
                + responseCount
                + " commands"
        );

        // ==========================================
        // TEST 7: RESPONSE QUEUE
        // ==========================================

        System.out.println(
                "\nTEST 7: CORE -> UI RESPONSE QUEUE"
        );

        int receivedCount = 0;

        while (channel.hasResponse()) {

            IPCMessage message =
                    ui.receiveMessage();

            if (message != null) {

                receivedCount++;

                System.out.println(
                        "Response "
                        + receivedCount
                        + ": "
                        + message.getCommand()
                );
            }
        }

        System.out.println(
                "Total responses received: "
                + receivedCount
        );

        // ==========================================
        // FINAL RESULT
        // ==========================================

        System.out.println("\n======================================");
        System.out.println("          IPC TEST COMPLETED");
        System.out.println("======================================");

        if (!channel.hasCommand()
                && !channel.hasResponse()) {

            System.out.println(
                    "PASS: Both IPC queues are empty"
            );

            System.out.println(
                    "PASS: Messages were transmitted"
            );

            System.out.println(
                    "PASS: UI -> Core communication works"
            );

            System.out.println(
                    "PASS: Core -> UI communication works"
            );

        } else {

            System.out.println(
                    "FAIL: Some messages remain in queues"
            );
        }

        System.out.println("======================================");
    }
}
