package src.ipc;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class IPCChannel {

    private final BlockingQueue<IPCMessage> messages;

    public IPCChannel() {
        messages = new LinkedBlockingQueue<>();
    }

    public void send(IPCMessage message) {
        messages.offer(message);
    }

    public IPCMessage receive() {
        return messages.poll();
    }

    public boolean hasMessage() {
        return !messages.isEmpty();
    }
}