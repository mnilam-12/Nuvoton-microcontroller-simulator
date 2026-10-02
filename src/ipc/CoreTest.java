package src.ipc;

import src.cpu.CPU;
import src.queue.FIFOQueue;

public class CoreProcess {

    private final IPCChannel channel;
    private final CPU cpu;

    public CoreProcess(IPCChannel channel) {
        this.channel = channel;
        this.cpu = new CPU();
    }

    public void processCommand() {

        IPCMessage message = channel.receiveFromUI();

        if (message == null) {
            return;
        }

        System.out.println("Core received: " + message.getCommand());
        System.out.println("Data: " + message.getData());

        try {

            switch (message.getCommand()) {

                case "LOAD":
                    cpu.reset();
                    cpu.loadProgram(message.getData());
                    sendResponse("Program loaded into Core CPU" + getCPUState());
                    break;

                case "RESET":
                    cpu.reset();
                    sendResponse("Core CPU RESET completed" + getCPUState());
                    break;

                case "STEP":
                    executeOneInstruction();
                    break;

                case "RUN":
                    runProgram();
                    break;

                default:
                    sendError("Unknown command: " + message.getCommand());
                    break;
            }

        } catch (Exception e) {
            sendError(e.getMessage() == null ? "Core execution error" : e.getMessage());
        }
    }

    private void executeOneInstruction() {

        if (cpu.isHalted()) {
            sendResponse("CPU HALTED" + getCPUState());
            return;
        }

        int fetchPC = cpu.getRegisters().getPC();

        cpu.fetch();

        String instruction =
                cpu.getCurrentInstruction() == null
                        ? "NONE"
                        : cpu.getCurrentInstruction().toString();

        cpu.decode();

        String decoded =
                cpu.getDecodedInstruction() == null
                        ? "NONE"
                        : cpu.getDecodedInstruction().toString();

        cpu.execute();

        String status = cpu.isHalted()
                ? "CPU HALTED"
                : "CPU step executed successfully";

        String response =
                status
                + " | FETCHPC=" + String.format("%04X", fetchPC)
                + " | INSTR=" + instruction
                + " | DECODE=" + decoded
                + getCPUState();

        sendResponse(response);
    }

    private void runProgram() {

        int count = 0;
        StringBuilder trace = new StringBuilder();

        while (!cpu.isHalted() && count < 100) {

            int fetchPC = cpu.getRegisters().getPC();

            cpu.fetch();

            String instruction =
                    cpu.getCurrentInstruction() == null
                            ? "NONE"
                            : cpu.getCurrentInstruction().toString();

            cpu.decode();

            String decoded =
                    cpu.getDecodedInstruction() == null
                            ? "NONE"
                            : cpu.getDecodedInstruction().toString();

            cpu.execute();

            if (trace.length() > 0) {
                trace.append("~");
            }

            trace.append(
                    String.format(
                            "%04X:%s:%s",
                            fetchPC,
                            instruction,
                            decoded
                    )
            );

            count++;
        }

        String response;

        if (cpu.isHalted()) {
            response = "CPU RUN completed - HALTED";
        } else {
            response = "CPU RUN stopped after 100 instructions";
        }

        response += " | RUNTRACE=" + trace;
        response += getCPUState();

        sendResponse(response);
    }

    private String getCPUState() {

        FIFOQueue queue = cpu.getQueue();

        StringBuilder queueData = new StringBuilder();

        for (int i = 0; i < queue.size(); i++) {

            int index =
                    (queue.getFront() + i)
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

            stackData.append(
                    String.format(
                            "%02X:%02X",
                            address,
                            cpu.getDataMemory().read(address)
                    )
            );
        }

        return
                " | PC="
                        + String.format("%04X", cpu.getRegisters().getPC())
                + " | ACC="
                        + String.format("%02X", cpu.getRegisters().getACC())
                + " | B="
                        + String.format("%02X", cpu.getRegisters().getB())
                + " | R0="
                        + String.format("%02X", cpu.getRegisters().getR(0))
                + " | R1="
                        + String.format("%02X", cpu.getRegisters().getR(1))
                + " | CY="
                        + (cpu.getFlags().isCarry() ? "1" : "0")
                + " | QDATA=" + queueData
                + " | QFRONT=" + queue.getFront()
                + " | QREAR=" + queue.getRear()
                + " | QSIZE=" + queue.size()
                + " | QSTATUS=" + queue.getStatus()
                + " | STACKDATA=" + stackData
                + " | SP=" + String.format("%02X", sp);
    }

    private void sendResponse(String data) {
        channel.sendToUI(
                new IPCMessage("RESPONSE", data)
        );
    }

    private void sendError(String error) {
        channel.sendToUI(
                new IPCMessage("ERROR", error)
        );
    }
}
