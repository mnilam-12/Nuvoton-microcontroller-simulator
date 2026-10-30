
package src.integration.multiprocess;

import java.net.Socket;

public class CoreClient {

    public static void main(String[] args) {

        try (Socket socket = new Socket("localhost", 5001)) {

            SocketIPC ipc = new SocketIPC(socket);

            String[] commands = {
                "LOAD MOV A, #05",
                "STEP",
                "RESET"
            };

            for (String command : commands) {
                System.out.println("Sending: " + command);

                ipc.send(command);

                String response = ipc.receive();
                System.out.println("Response: " + response);
            }

            System.out.println("Client test completed.");

        } catch (Exception e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }
}
