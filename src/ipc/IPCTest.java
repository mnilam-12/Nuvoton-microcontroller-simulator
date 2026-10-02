package src.ipc;

public class IPCTest {

    public static void main(String[] args) {

        IPCChannel channel = new IPCChannel();

        UIProcess ui = new UIProcess(channel);
        CoreProcess core = new CoreProcess(channel);

        // UI requests one CPU step
        ui.sendCommand("STEP", "Execute one instruction");

        // Core receives and processes the request
        core.processCommand();

        // UI receives response
        IPCMessage response = ui.receiveMessage();

        if (response != null) {
            System.out.println("UI received: " + response.getCommand());
            System.out.println("Data: " + response.getData());
        }
    }
}