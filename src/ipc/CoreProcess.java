package src.ipc;
import src.log.LoggerProcess;
import src.cpu.CPU;

public class CoreProcess implements Runnable {

    private IPCChannel channel;
    private CPU cpu;
    private volatile boolean running = true;

    // Constructor
    public CoreProcess(IPCChannel channel) {
        this.channel = channel;
        this.cpu = new CPU();
    }

    // Start the core process in a separate thread
    public static CoreProcess startCore(IPCChannel channel) {

        CoreProcess coreProcess = new CoreProcess(channel);

        Thread coreThread = new Thread(coreProcess);
        coreThread.start();

        return coreProcess;
    }

    // This method runs in the new thread
    @Override
    public void run() {

        LoggerProcess.info("Core thread started");

        while (running) {
            try {
                // Wait until the UI sends a command
                IPCMessage message = channel.receiveFromUIBlocking();

                if (message != null) {
                    processCommand(message);
                }

            } catch (InterruptedException e) {
                running = false;
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("Core thread stopped");
    }

    // Process commands received from the UI
private void processCommand(IPCMessage message) {

    String command = message.getCommand();
    String data = message.getData();

    // Log the command received by the core
    LoggerProcess.info("Core received command: " + command);

    try {

        if (command.equals("LOAD")) {

            cpu.reset();
            cpu.loadProgram(data);

            LoggerProcess.info("Program loaded successfully");
            sendResponse("Program loaded successfully");

        } else if (command.equals("RESET")) {

            cpu.reset();

            LoggerProcess.info("CPU reset completed");
            sendResponse("CPU reset completed");

        } else if (command.equals("STEP")) {

            LoggerProcess.info("Executing one instruction");
            executeOneInstruction();

        } else if (command.equals("RUN")) {

            LoggerProcess.info("Starting program execution");
            runProgram();

        } else if (command.equals("STOP")) {

            running = false;

            LoggerProcess.info("Core process stopped");
            sendResponse("Core process stopped");

        } else {

            LoggerProcess.warning("Unknown command: " + command);
            sendError("Unknown command: " + command);
        }

    } catch (Exception e) {

        LoggerProcess.error(
            "Error processing command " + command
            + ": " + e.getMessage()
        );

        sendError("Error: " + e.getMessage());
    }
}



    // Execute one instruction
    private void executeOneInstruction() {

        if (cpu.isHalted()) {
            sendResponse("CPU HALTED");
            return;
        }

        int oldPC = cpu.getRegisters().getPC();

        cpu.fetch();

        String instruction = "NONE";

        if (cpu.getCurrentInstruction() != null) {
            instruction = cpu.getCurrentInstruction().toString();
        }

        cpu.decode();
        cpu.execute();

        int newPC = cpu.getRegisters().getPC();
        int acc = cpu.getRegisters().getACC();

        String response =
                "Executed: " + instruction
                + " | Previous PC: " + oldPC
                + " | Current PC: " + newPC
                + " | ACC: " + acc;

        System.out.println(response);

        sendResponse(response);
    }

    // Run instructions until halted or 100 instructions have executed
    private void runProgram() {

        int count = 0;

        while (!cpu.isHalted() && count < 100) {

            executeOneInstruction();

            count++;
        }

        if (cpu.isHalted()) {

            System.out.println("Program completed");
            sendResponse("Program completed");

        } else {

            System.out.println("Stopped after 100 instructions");
            sendResponse("Stopped after 100 instructions");
        }
    }

    // Send a response to the UI
    private void sendResponse(String data) {

        IPCMessage message = new IPCMessage("RESPONSE", data);

        channel.sendToUI(message);
    }

    // Send an error to the UI
    private void sendError(String error) {

        IPCMessage message = new IPCMessage("ERROR", error);

        channel.sendToUI(message);
    }
}