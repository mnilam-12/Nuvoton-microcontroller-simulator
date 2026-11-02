package src.log;

import src.ipc.IPCChannel;
import src.ipc.IPCMessage;

public class LoggerReceiver implements Runnable {

    private final IPCChannel channel;

    public LoggerReceiver(IPCChannel channel) {
        this.channel = channel;
    }

    @Override
    public void run() {
        LoggerProcess.info("Logger receiver started.");

        try {
            while (!Thread.currentThread().isInterrupted()) {

                IPCMessage message =
                    channel.receiveFromLoggerBlocking();

                String level = message.getCommand();
                String data = message.getData();

                LoggerProcess.log(level, data);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LoggerProcess.info("Logger receiver stopped.");
        }
    }
}
