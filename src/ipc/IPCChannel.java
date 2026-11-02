package src.ipc;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class IPCChannel {

    private final BlockingQueue<IPCMessage> uiToCore;
    private final BlockingQueue<IPCMessage> coreToUI;

    public IPCChannel() {

        uiToCore = new LinkedBlockingQueue<>();
        coreToUI = new LinkedBlockingQueue<>();

        System.out.println("[IPC] Channel created");
    }

    // UI → CORE
    public void sendToCore(IPCMessage message) {

        if (message == null) {
            throw new IllegalArgumentException(
                "Cannot send a null message"
            );
        }

        uiToCore.offer(message);

        System.out.println(
            "[IPC] UI sent: " + message.getCommand()
        );
    }

    public IPCMessage receiveFromUI() {

        return uiToCore.poll();
    }

    public IPCMessage receiveFromUIBlocking()
            throws InterruptedException {

        System.out.println("[IPC] Core waiting for UI");

        IPCMessage message = uiToCore.take();

        System.out.println(
            "[IPC] Core received: " + message.getCommand()
        );

        return message;
    }

    // CORE → UI
    public void sendToUI(IPCMessage message) {

        if (message == null) {
            throw new IllegalArgumentException(
                "Cannot send a null response"
            );
        }

        coreToUI.offer(message);

        System.out.println(
            "[IPC] Core sent: "
            + message.getCommand()
            + " | " + message.getData()
        );
    }

    public IPCMessage receiveFromCore() {

        IPCMessage message = coreToUI.poll();

        if (message != null) {

            System.out.println(
                "[IPC] UI received: "
                + message.getCommand()
                + " | " + message.getData()
            );
        }

        return message;
    }
// ==============================
// UI → LOGGER
// ==============================

private final BlockingQueue<IPCMessage> uiToLogger =
        new LinkedBlockingQueue<>();

public void sendToLogger(IPCMessage message) {
    uiToLogger.offer(message);
}

public IPCMessage receiveFromLogger() {
    return uiToLogger.poll();
}

public IPCMessage receiveFromLoggerBlocking()
        throws InterruptedException {
    return uiToLogger.take();
}


    public boolean hasCommand() {
        return !uiToCore.isEmpty();
    }

    public boolean hasResponse() {
        return !coreToUI.isEmpty();
    }
}