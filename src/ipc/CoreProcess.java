package src.ipc;

import src.cpu.CPU;

public class CoreProcess {

    private final IPCChannel channel;
    private final CPU cpu;

    public CoreProcess(IPCChannel channel) {
        this.channel = channel;
        this.cpu = new CPU();
    }

    public void processCommand() {

        IPCMessage message = channel.receive();

        if (message != null) {

            System.out.println(
                    "Core received: " + message.getCommand()
            );

            System.out.println(
                    "Data: " + message.getData()
            );

            if (message.getCommand().equals("LOAD")) {

                try {

                    cpu.reset();

                    cpu.loadProgram(
                            message.getData()
                    );

                    IPCMessage response =
                            new IPCMessage(
                                    "RESPONSE",
                                    "Program loaded into Core CPU"
                            );

                    channel.send(response);

                } catch (Exception e) {

                    IPCMessage response =
                            new IPCMessage(
                                    "ERROR",
                                    e.getMessage()
                            );

                    channel.send(response);
                }

            } else if (message.getCommand().equals("STEP")) {

                try {

                    // Core Process controls the CPU
                    cpu.fetch();
cpu.decode();
cpu.execute();

String responseData;

if (cpu.isHalted()) {

    responseData =
            "CPU HALTED";

} else {

    responseData =
            "CPU step executed successfully";
}

responseData +=
        " | PC="
        + String.format(
                "%04X",
                cpu.getRegisters().getPC()
        )
        + " | ACC="
        + String.format(
                "%02X",
                cpu.getRegisters().getACC()
        )
        + " | B="
        + String.format(
                "%02X",
                cpu.getRegisters().getB()
        )
        + " | R0="
        + String.format(
                "%02X",
                cpu.getRegisters().getR(0)
        )
        + " | R1="
        + String.format(
                "%02X",
                cpu.getRegisters().getR(1)
        )
        + " | CY="
        + (
                cpu.getFlags().isCarry()
                        ? "1"
                        : "0"
        )
        + " | SP="
        + String.format(
                "%02X",
                cpu.getStackPointer().getValue()
        );

                    IPCMessage response =
                            new IPCMessage(
                                    "RESPONSE",
                                    responseData
                            );

                    channel.send(response);

                } catch (Exception e) {

                    IPCMessage response =
                            new IPCMessage(
                                    "ERROR",
                                    e.getMessage()
                            );

                    channel.send(response);
                }

            } else if (message.getCommand().equals("RUN")) {

                IPCMessage response =
                        new IPCMessage(
                                "RESPONSE",
                                "CPU RUN command received"
                        );

                channel.send(response);
            }
        }
    }
}