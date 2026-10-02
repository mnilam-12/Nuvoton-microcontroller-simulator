package src.ipc;

public class UIProcess {

    private final IPCChannel channel;

    public UIProcess(IPCChannel channel) {
        this.channel = channel;
    }

    public void sendCommand(String command, String data) {
        IPCMessage message = new IPCMessage(command, data);
        channel.send(message);
    }

    public IPCMessage receiveMessage() {
        return channel.receive();
    }
}