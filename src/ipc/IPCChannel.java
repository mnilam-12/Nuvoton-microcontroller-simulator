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
    // CHECK MESSAGE
    // ==============================

    public boolean hasCommand() {

        return !uiToCore.isEmpty();
    }

    public boolean hasResponse() {

        return !coreToUI.isEmpty();
    }
}
