
package src.integration.multiprocess;

import java.net.Socket;

public class LoggerClient {

    public static void main(String[] args) {

        try (Socket socket = new Socket("localhost", 5002)) {

            SocketIPC ipc = new SocketIPC(socket);

            String[] messages = {
                "INFO:UI connected successfully",
                "WARNING:Test warning message",
                "ERROR:Test error message"
            };

            for (String message : messages) {

                ipc.send(message);

                System.out.println("Sent: " + message);
                System.out.println("Response: " + ipc.receive());
            }

            System.out.println("Logger IPC test completed.");

        } catch (Exception e) {
            System.err.println("Logger client error: " + e.getMessage());
        }
    }
}
