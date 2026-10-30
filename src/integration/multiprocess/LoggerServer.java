
package src.integration.multiprocess;

import src.log.LoggerProcess;
import java.net.ServerSocket;
import java.net.Socket;

public class LoggerServer {

    public static void main(String[] args) {

        try (ServerSocket server = new ServerSocket(5002)) {

            System.out.println("Logger Server started on port 5002");

            while (true) {

                try (Socket socket = server.accept()) {

                    SocketIPC ipc = new SocketIPC(socket);
                    String message;

                    while ((message = ipc.receive()) != null) {

                        if (message.equals("STOP")) {
                            ipc.send("Logger Server stopping");
                            return;
                        }

                        String level = "INFO";
                        String content = message;

                        if (message.startsWith("ERROR:")) {
                            level = "ERROR";
                            content = message.substring(6).trim();
                        } else if (message.startsWith("WARNING:")) {
                            level = "WARNING";
                            content = message.substring(8).trim();
                        } else if (message.startsWith("INFO:")) {
                            content = message.substring(5).trim();
                        }

                        LoggerProcess.log(level, content);
                        ipc.send("Log saved");
                    }
                }

            }

        } catch (Exception e) {
            System.err.println(
                "Logger Server error: " + e.getMessage()
            );
        }
    }
}
