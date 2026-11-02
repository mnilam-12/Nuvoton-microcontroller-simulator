package src.ipc;

import src.cpu.CPU;
import src.log.LoggerProcess;

public class CoreProcess implements Runnable {

    private final IPCChannel channel;
    private final CPU cpu;
    private final LoggerProcess logger;

    private volatile boolean running = true;

    private static final int MAX_INSTRUCTIONS = 100;

    // Constructor
    public CoreProcess(
            IPCChannel channel,
            LoggerProcess logger,
            CPU cpu) {

        this.channel = channel;
        this.logger = logger;
        this.cpu = cpu;
    }

    // Start Core thread
    public static CoreProcess startCore(
            IPCChannel channel,
            LoggerProcess logger,
            CPU cpu) {

        CoreProcess core = new CoreProcess(
                channel, logger, cpu
        );

        Thread coreThread = new Thread(
                core, "Core-Thread"
        );

        System.out.println("[CORE] Starting Core thread");

        coreThread.start();

        return core;
    }

    // Compatibility with older Benchmark.java
    public static CoreProcess startCore(
            IPCChannel channel,
            LoggerProcess logger) {

        return startCore(channel, logger, new CPU());
    }

    // Compatibility with older IntegrationTest.java
    public static CoreProcess startCore(
            IPCChannel channel) {

        return startCore(channel, null, new CPU());
    }
    // ==========================================
// LOGGER METHODS
// ==========================================

private void logInfo(String message) {

    if (logger != null) {
        logger.info(message);
    }
}

private void logWarning(String message) {

    if (logger != null) {
        logger.warning(message);
    }
}

private void logError(String message) {

    if (logger != null) {
        logger.error(message);
    }
}

    // ==========================================
    // CORE THREAD
    // ==========================================

    @Override
    public void run() {

        System.out.println("[CORE] run() entered");

        try {

            while (running) {

                System.out.println(
                        "[CORE] Waiting for UI command..."
                );

                // Wait for UI message
                IPCMessage message =
                        channel.receiveFromUIBlocking();

                if (message != null) {
                    logInfo(
    "Core received command: " + message.getCommand()
              );

                    System.out.println(
                            "[CORE] Message received: "
                            + message.getCommand()
                    );

                    processCommand(message);
                }
            }

        } catch (InterruptedException e) {

            running = false;

            Thread.currentThread().interrupt();

            System.out.println("[CORE] Thread interrupted");

        } catch (Exception e) {

            System.err.println(
                    "[CORE] Thread error: " + e.getMessage()
            );

            e.printStackTrace();

        } finally {

            System.out.println("[CORE] Core thread stopped");
            logInfo("Core thread stopped");
        }
    }

    // ==========================================
    // PROCESS COMMAND
    // ==========================================

    private void processCommand(IPCMessage message) {

        if (message == null) {
            return;
        }

        String command = message.getCommand();
        String data = message.getData();

        if (command == null) {

            sendError("Command cannot be null");
            return;
        }

        command = command.trim().toUpperCase();
        logInfo("Processing command: " + command);

        System.out.println(
                "[CORE] Processing command: " + command
        );

        try {

            switch (command) {

                // LOAD PROGRAM
                case "LOAD":
                    logInfo("Program loaded successfully");
                    System.out.println("[CORE] Loading program");

                    if (data == null || data.trim().isEmpty()) {

                        throw new IllegalArgumentException(
                                "Program is empty"
                        );
                    }

                    cpu.reset();

                    cpu.loadProgram(data);
                    

                    sendResponse(
                            "Program loaded successfully"
                    );

                    break;

                // RESET CPU
                case "RESET":

                    cpu.reset();
                    logInfo("CPU reset completed");

                    sendResponse(
                            "CPU reset completed"
                    );

                    break;

                // STEP
                case "STEP":

                    String stepResult =
                            executeOneInstruction();

                    sendResponse(stepResult);

                    break;

                // RUN
                case "RUN":

                    runProgram();

                    break;

                // PING
                case "PING":

                    System.out.println(
                            "[CORE] PING received from UI"
                    );
                    logInfo("IPC TEST SUCCESSFUL: Core received PING");

                    // Send PONG immediately
                    sendResponse("PONG");

                    System.out.println(
                            "[CORE] PONG sent to UI"
                    );

                    break;

                // STOP
                case "STOP":

                    sendResponse(
                            "Core process stopped"
                    );

                    running = false;

                    break;

                default:

                    sendError(
                            "Unknown command: " + command
                    );

                    break;
            }

        } catch (Exception e) {

            String error = e.getMessage() != null
                    ? e.getMessage()
                    : e.toString();

            System.err.println(
                    "[CORE ERROR] " + error
            );

            sendError(error);
        }
    }

    // ==========================================
    // EXECUTE ONE INSTRUCTION
    // ==========================================

    private String executeOneInstruction() {

        if (cpu.isHalted()) {
            return "CPU HALTED";
        }

        int oldPC = cpu.getRegisters().getPC();

        // FETCH
        cpu.fetch();

        if (cpu.getCurrentInstruction() == null) {

            throw new IllegalStateException(
                    "No instruction found at PC: " + oldPC
            );
        }

        String instruction =
                cpu.getCurrentInstruction().toString();

        // DECODE
        cpu.decode();

        String decoded =
                cpu.getDecodedInstruction().toString();

        // EXECUTE
        cpu.execute();

        int newPC = cpu.getRegisters().getPC();

        int accumulator =
                cpu.getRegisters().getACC();

        String result =
                "Executed: " + instruction
                + " | Decoded: " + decoded
                + " | Previous PC: " + oldPC
                + " | Current PC: " + newPC
                + " | ACC: " + accumulator;

        System.out.println("[CORE] " + result);
        logInfo(result);
        return result;
    }

    // ==========================================
    // RUN PROGRAM
    // ==========================================

    private void runProgram() {

        int count = 0;

        System.out.println("[CORE] Program execution started");
        logInfo("Core thread started");
        
        while (!cpu.isHalted()
                && count < MAX_INSTRUCTIONS) {

            executeOneInstruction();

            count++;
        }

        if (cpu.isHalted()) {

            sendResponse(
                    "Program completed after "
                    + count + " instructions"
            );
            logInfo("Program completed successfully");

        } else {

            sendResponse(
                    "Execution stopped after "
                    + count + " instructions"
            );
            logWarning("Program stopped after 100 instructions");
        }
    }

    // ==========================================
    // SEND RESPONSE TO UI
    // ==========================================

    private void sendResponse(String data) {

        IPCMessage response =
                new IPCMessage("RESPONSE", data);

        channel.sendToUI(response);

        System.out.println(
                "[CORE] Response sent: " + data
        );
        logInfo("Response sent to UI: " + data);
    }

    // ==========================================
    // SEND ERROR TO UI
    // ==========================================

    private void sendError(String error) {

        IPCMessage response =
                new IPCMessage("ERROR", error);

        channel.sendToUI(response);

        System.err.println(
                "[CORE] Error sent: " + error
        );
         logError("Core error sent to UI: " + error);
    }

    // ==========================================
    // GETTERS
    // ==========================================

    public CPU getCPU() {

        return cpu;
    }

    public boolean isRunning() {

        return running;
    }
}