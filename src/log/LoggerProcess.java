package src.log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggerProcess {

    private static final Path LOG_FILE =
            Paths.get("logs", "simulator.log");

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Create the log folder if it does not exist.
    private static void createLogFolder() throws IOException {
        Files.createDirectories(LOG_FILE.getParent());
    }

    // Save a message with its level and timestamp.
    public static synchronized void log(
            String level, String message) {

        try {
            createLogFolder();

            String timestamp =
                    LocalDateTime.now().format(FORMATTER);

            String entry = "[" + timestamp + "] ["
                    + level + "] " + message;

            try (BufferedWriter writer = Files.newBufferedWriter(
                    LOG_FILE,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND)) {

                writer.write(entry);
                writer.newLine();
           }

            System.out.println(entry);

        } catch (IOException e) {
            System.err.println(
                    "Unable to write log: " + e.getMessage());
        }
    }

    public static void info(String message) {
        log("INFO", message);
    }

    public static void warning(String message) {
        log("WARNING", message);
    }

    public static void error(String message) {
        log("ERROR", message);
    }

    // Test the logger independently.
    public static void main(String[] args) {
        info("Logger started.");
        info("Simulator UI is ready.");
        warning("Program has not been loaded.");
        error("Test error message.");

        System.out.println(
                "Logging test completed. Check logs/simulator.log");
    }
}
