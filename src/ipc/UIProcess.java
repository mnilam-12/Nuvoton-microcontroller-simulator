package src.ipc;

public class UIProcess {

    // IPC channel used to communicate with Core
    private final IPCChannel channel;

    // Constructor
    public UIProcess(IPCChannel channel) {

        this.channel = channel;
    }

    // =================================
    // SEND COMMAND TO CORE
    // =================================

    public void sendCommand(
            String command,
            String data) {

        // Create message
        IPCMessage message =
                new IPCMessage(
                        command,
                        data
                );

        // Send message to Core
        channel.sendToCore(message);
    }

    // =================================
    // RECEIVE RESPONSE FROM CORE
    // =================================

    public IPCMessage receiveMessage() {

        // Receive message from Core
        return channel.receiveFromCore();
    }
}
