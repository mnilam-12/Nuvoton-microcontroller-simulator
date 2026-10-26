package src.ipc;

public class CoreTest {

    public static void main(String[] args) {

        System.out.println("======================================");
        System.out.println("          COMPLETE CORE TEST");
        System.out.println("======================================");

        IPCChannel channel = new IPCChannel();

        CoreProcess core = new CoreProcess(channel);

        // ==========================================
        // TEST 1: LOAD
        // ==========================================

        System.out.println("\nTEST 1: LOAD PROGRAM");

        String program =
                "MOV A,#05\n" +
                "ADD A,#03\n" +
                "HALT";

        channel.sendToCore(
                new IPCMessage("LOAD", program)
        );

        core.processCommand();

        IPCMessage response = channel.receiveFromCore();

        printResponse(response);


        // ==========================================
        // TEST 2: FIRST STEP
        // MOV A,#05
        // ==========================================

        System.out.println("\nTEST 2: STEP - MOV");

        channel.sendToCore(
                new IPCMessage(
                        "STEP",
                        "Execute MOV A,#05"
                )
        );

        core.processCommand();

        response = channel.receiveFromCore();

        printResponse(response);


        // ==========================================
        // TEST 3: SECOND STEP
        // ADD A,#03
        // ==========================================

        System.out.println("\nTEST 3: STEP - ADD");

        channel.sendToCore(
                new IPCMessage(
                        "STEP",
                        "Execute ADD A,#03"
                )
        );

        core.processCommand();

        response = channel.receiveFromCore();

        printResponse(response);


        // ==========================================
        // TEST 4: THIRD STEP
        // HALT
        // ==========================================

        System.out.println("\nTEST 4: STEP - HALT");

        channel.sendToCore(
                new IPCMessage(
                        "STEP",
                        "Execute HALT"
                )
        );

        core.processCommand();

        response = channel.receiveFromCore();

        printResponse(response);


        // ==========================================
        // TEST 5: RESET
        // ==========================================

        System.out.println("\nTEST 5: RESET");

        channel.sendToCore(
                new IPCMessage(
                        "RESET",
                        "Reset CPU"
                )
        );

        core.processCommand();

        response = channel.receiveFromCore();

        printResponse(response);


        // ==========================================
        // TEST 6: RUN
        // ==========================================

        System.out.println("\nTEST 6: RUN");

        String runProgram =
                "MOV A,#10\n" +
                "ADD A,#20\n" +
                "HALT";

        channel.sendToCore(
                new IPCMessage(
                        "LOAD",
                        runProgram
                )
        );

        core.processCommand();

        response = channel.receiveFromCore();

        printResponse(response);

        // Send RUN command

        channel.sendToCore(
                new IPCMessage(
                        "RUN",
                        "Run program"
                )
        );

        core.processCommand();

        response = channel.receiveFromCore();

        printResponse(response);


        // ==========================================
        // TEST 7: UNKNOWN COMMAND
        // ==========================================

        System.out.println("\nTEST 7: UNKNOWN COMMAND");

        channel.sendToCore(
                new IPCMessage(
                        "ABC",
                        "Invalid command"
                )
        );

        core.processCommand();

        response = channel.receiveFromCore();

        printResponse(response);


        // ==========================================
        // TEST 8: INVALID PROGRAM
        // ==========================================

        System.out.println("\nTEST 8: INVALID PROGRAM");

        String invalidProgram =
                "INVALID_INSTRUCTION";

        channel.sendToCore(
                new IPCMessage(
                        "LOAD",
                        invalidProgram
                )
        );

        core.processCommand();

        response = channel.receiveFromCore();

        printResponse(response);


        // ==========================================
        // FINAL RESULT
        // ==========================================

        System.out.println("\n======================================");
        System.out.println("       COMPLETE CORE TEST FINISHED");
        System.out.println("======================================");
    }


    // ==========================================
    // PRINT RESPONSE
    // ==========================================

    private static void printResponse(
            IPCMessage response) {

        if (response == null) {

            System.out.println(
                    "FAIL: No response received"
            );

        } else {

            System.out.println(
                    "Response Type: "
                    + response.getCommand()
            );

            System.out.println(
                    "Response Data: "
                    + response.getData()
            );
        }
    }
}