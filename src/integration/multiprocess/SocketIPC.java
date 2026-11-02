
package src.integration.multiprocess;

import java.io.*;
import java.net.*;

public class SocketIPC {

    private final Socket socket;
    private final BufferedReader reader;
    private final PrintWriter writer;

    public SocketIPC(Socket socket) throws IOException {
        this.socket = socket;

        reader = new BufferedReader(
            new InputStreamReader(
                socket.getInputStream()
            )
        );

        writer = new PrintWriter(
            socket.getOutputStream(), true
        );
    }

    // Send a message to another process
    public void send(String message) {
        writer.println(message);
    }

    // Receive a message from another process
    public String receive() throws IOException {
        return reader.readLine();
    }

    // Close the connection
    public void close() throws IOException {
        socket.close();
    }
}
