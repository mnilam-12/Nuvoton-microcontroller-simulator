package src.ipc;

public class IPCMessage {

    private String command;
    private String data;

    public IPCMessage(String command, String data) {
        this.command = command;
        this.data = data;
    }

    public String getCommand() {
        return command;
    }

    public String getData() {
        return data;
    }
}
