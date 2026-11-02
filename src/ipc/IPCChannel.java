package src.ipc;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class IPCChannel {

    // Queue for messages going from UI to Core
    private final BlockingQueue<IPCMessage> uiToCore;

    // Queue for messages going from Core to UI
    private final BlockingQueue<IPCMessage> coreToUI;

    // Constructor
    public IPCChannel() {

        uiToCore = new LinkedBlockingQueue<>();

        coreToUI = new LinkedBlockingQueue<>();
    }

    // ==============================
    // UI → CORE
    // ==============================

    public void sendToCore(IPCMessage message) {

        uiToCore.offer(message);
    }

    public IPCMessage receiveFromUI() {

        return uiToCore.poll();
    }
    public IPCMessage receiveFromUIBlocking() throws InterruptedException {
    return uiToCore.take();
    }

    // ==============================
    // CORE → UI
    // ==============================

    public void sendToUI(IPCMessage message) {

        coreToUI.offer(message);
    }

    public IPCMessage receiveFromCore() {

        return coreToUI.poll();
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


    // ==============================
    // CHECK MESSAGE
    // ==============================

    public boolean hasCommand() {

        return !uiToCore.isEmpty();
    }

    public boolean hasResponse() {

        return !coreToUI.isEmpty();
    }
}
