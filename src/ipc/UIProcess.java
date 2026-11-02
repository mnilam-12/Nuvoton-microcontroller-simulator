package src.ipc;
import src.log.LoggerProcess;
public class UIProcess {

    // IPC channel used to communicate with Core
    private final IPCChannel channel;
    private LoggerProcess logger;

    // Constructor
    public UIProcess(IPCChannel channel,LoggerProcess logger) {

        this.channel = channel;
        this.logger = logger;
    }

    // =================================
    // SEND COMMAND TO CORE
    // =================================

    public synchronized void sendCommand(String command, String data) {
    logger.info("UI sending command: " + command);

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
