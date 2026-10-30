
package src.integration.multiprocess;

import src.cpu.CPU;
import java.net.ServerSocket;
import java.net.Socket;

public class CoreServer {

    public static void main(String[] args) {

        CPU cpu = new CPU();

        try (ServerSocket server = new ServerSocket(5001)) {

            System.out.println("Core process started on port 5001");

            while (true) {

                try (Socket socket = server.accept()) {

                    SocketIPC ipc = new SocketIPC(socket);
                    System.out.println("Client connected");

                    String message;

                    while ((message = ipc.receive()) != null) {
                         sendLog("INFO", "Core received command: " + message);
                        try {
                            String response;
                            

                            switch (message) {

                                case "RESET":
                                    cpu.reset();
                                    response = "CPU reset completed";
                                    break;

                                case "STEP":
                                    if (cpu.isHalted()) {
                                        response = "CPU HALTED";
                                    } else {
                                        cpu.step();
                                        response = "STEP completed";
                                    }
                                    break;

                                case "RUN":
                                    int count = 0;

                                    while (!cpu.isHalted() && count < 100) {
                                        
                                        cpu.step();
                                        count++;
                                    }

                                    response = cpu.isHalted()
                                            ? "Program completed"
                                            : "Stopped after 100 instructions";
                                    break;

                                case "STOP":
                                    ipc.send("Core process stopping");
                                    return;

                                default:
                                    if (message.startsWith("LOAD ")) {
                                        cpu.reset();
                                        cpu.loadProgram(
                                            message.substring(5)
                                        );
                                        response = "Program loaded successfully";
                                    } else {
                                        response = "Unknown command";
                                    }
                            }

                            ipc.send(response);

                        } catch (Exception e) {
                            ipc.send("ERROR: " + e.getMessage());
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Core server error: " + e.getMessage());
        }
    }
    
private static void sendLog(String level, String message) {
    try (Socket socket = new Socket("localhost", 5002)) {
        SocketIPC logger = new SocketIPC(socket);
        logger.send(level + ":" + message);
        logger.receive();
    } catch (Exception e) {
        System.err.println("Logger connection failed: "
                + e.getMessage());
    }
}

}
