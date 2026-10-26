package src.ipc;
import src.log.LoggerProcess;
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

    public void sendCommand(String command, String data) {
    LoggerProcess.info("UI sending command: " + command);

    IPCMessage message = new IPCMessage(command, data);
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
