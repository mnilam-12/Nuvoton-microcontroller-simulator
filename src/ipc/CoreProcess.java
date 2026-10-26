package src.ipc;

import src.cpu.CPU;
import src.queue.FIFOQueue;

public class CoreProcess implements Runnable {

    private IPCChannel channel;
    private CPU cpu;
    private boolean running = true;

    // Constructor
    public CoreProcess(IPCChannel channel) {
        this.channel = channel;
        this.cpu = new CPU();
    }

    // This method runs in a separate thread
    @Override
    public void run() {
        while (running) {
            try {
                IPCMessage message = channel.receiveFromUIBlocking();

                if (message != null) {
                    processCommand(message);
                }

            } catch (InterruptedException e) {
                running = false;
            }
        }
    }

    // Process the command received from the UI
    private void processCommand(IPCMessage message) {

        String command = message.getCommand();
        String data = message.getData();

        System.out.println("Core received: " + command);

        try {
            switch (command) {

                case "LOAD":
                    cpu.reset();
                    cpu.loadProgram(data);
                    sendResponse("Program loaded" + getCPUState());
                    break;

                case "RESET":
                    cpu.reset();
                    sendResponse("CPU reset completed" + getCPUState());
                    break;

                case "STEP":
                    executeOneInstruction();
                    break;

                case "RUN":
                    runProgram();
                    break;

                case "STOP":
                    running = false;
                    break;

                default:
                    sendError("Unknown command: " + command);
                    break;
            }

        } catch (Exception e) {
            sendError("Error: " + e.getMessage());
        }
    }

    // Execute one instruction
    private void executeOneInstruction() {

        if (cpu.isHalted()) {
            sendResponse("CPU HALTED" + getCPUState());
            return;
        }

        int fetchPC = cpu.getRegisters().getPC();

        cpu.fetch();

        String instruction = "NONE";

        if (cpu.getCurrentInstruction() != null) {
            instruction = cpu.getCurrentInstruction().toString();
        }

        cpu.decode();

        String decoded = "NONE";

        if (cpu.getDecodedInstruction() != null) {
            decoded = cpu.getDecodedInstruction().toString();
        }

        cpu.execute();

        String status = "CPU step executed successfully";

        if (cpu.isHalted()) {
            status = "CPU HALTED";
        }

        String response = status
                + " | FETCHPC=" + String.format("%04X", fetchPC)
                + " | INSTR=" + instruction
                + " | DECODE=" + decoded
                + getCPUState();

        sendResponse(response);
    }

    // Run the program
    private void runProgram() {

        int count = 0;
        StringBuilder trace = new StringBuilder();

        while (!cpu.isHalted() && count < 100) {

            int fetchPC = cpu.getRegisters().getPC();

            cpu.fetch();

            String instruction = "NONE";

            if (cpu.getCurrentInstruction() != null) {
                instruction = cpu.getCurrentInstruction().toString();
            }

            cpu.decode();

            String decoded = "NONE";

            if (cpu.getDecodedInstruction() != null) {
                decoded = cpu.getDecodedInstruction().toString();
            }

            cpu.execute();

            if (trace.length() > 0) {
                trace.append("~");
            }

            trace.append(String.format(
                    "%04X:%s:%s",
                    fetchPC,
                    instruction,
                    decoded
            ));

            count++;
        }

        String response;

        if (cpu.isHalted()) {
            response = "CPU RUN completed - HALTED";
        } else {
            response = "CPU RUN stopped after 100 instructions";
        }

        response = response + " | RUNTRACE=" + trace;
        response = response + getCPUState();

        sendResponse(response);
    }

    // Get the current CPU information
    private String getCPUState() {

        FIFOQueue queue = cpu.getQueue();
        StringBuilder queueData = new StringBuilder();

        for (int i = 0; i < queue.size(); i++) {

            int index = (queue.getFront() + i)
                    % queue.getCapacity();

            if (i > 0) {
                queueData.append(",");
            }

            queueData.append(
                    String.format("%02X", queue.getValue(index))
            );
        }

        int sp = cpu.getStackPointer().getValue();

        StringBuilder stackData = new StringBuilder();

        int start = Math.max(0, sp - 15);
        int end = Math.min(255, sp + 15);

        for (int address = start; address <= end; address++) {

            if (stackData.length() > 0) {
                stackData.append(",");
            }

            stackData.append(String.format(
                    "%02X:%02X",
                    address,
                    cpu.getDataMemory().read(address)
            ));
        }

        return " | PC=" + String.format("%04X",
                    cpu.getRegisters().getPC())
                + " | ACC=" + String.format("%02X",
                    cpu.getRegisters().getACC())
                + " | B=" + String.format("%02X",
                    cpu.getRegisters().getB())
                + " | R0=" + String.format("%02X",
                    cpu.getRegisters().getR(0))
                + " | R1=" + String.format("%02X",
                    cpu.getRegisters().getR(1))
                + " | CY=" + (cpu.getFlags().isCarry() ? "1" : "0")
                + " | QDATA=" + queueData
                + " | QFRONT=" + queue.getFront()
                + " | QREAR=" + queue.getRear()
                + " | QSIZE=" + queue.size()
                + " | QSTATUS=" + queue.getStatus()
                + " | STACKDATA=" + stackData
                + " | SP=" + String.format("%02X", sp);
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