package src.log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LoggerProcess implements Runnable {

    private static final Path LOG_FILE =
            Paths.get("logs", "simulator.log");

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Queue for storing log messages
    private final BlockingQueue<LogEntry> logQueue =
            new LinkedBlockingQueue<>();

    private Thread loggerThread;

    private boolean accepting = true;

    // Log entry structure
    private static class LogEntry {

        String message;
        boolean stop;

        LogEntry(String message, boolean stop) {
            this.message = message;
            this.stop = stop;
        }
    }

    // ==========================================
    // START LOGGER THREAD
    // ==========================================

    public synchronized void start() {

        if (loggerThread != null) {
            return;
        }

        loggerThread = new Thread(
                this,
                "Logger-Thread"
        );

        loggerThread.start();
    }

    // ==========================================
    // ADD LOG MESSAGE
    // ==========================================

    private synchronized void log(
            String level,
            String message) {

        if (!accepting) {
            return;
        }

        String timestamp =
                LocalDateTime.now().format(FORMATTER);

        String entry =
                "[" + timestamp + "] "
                + "[" + level + "] "
                + message;

        logQueue.offer(
                new LogEntry(entry, false)
        );
    }

    // ==========================================
    // LOG LEVELS
    // ==========================================

    public void info(String message) {

        log("INFO", message);
    }

    public void warning(String message) {

        log("WARNING", message);
    }

    public void error(String message) {

        log("ERROR", message);
    }

    // ==========================================
    // LOGGER THREAD EXECUTION
    // ==========================================

    @Override
    public void run() {

        // IMPORTANT: Do not use synchronized here.

        System.out.println("Logger thread started");

        try {

            Files.createDirectories(
                    LOG_FILE.getParent()
            );

            System.out.println(
                    "Log file location: "
                    + LOG_FILE.toAbsolutePath()
            );

            try (BufferedWriter writer =
                    Files.newBufferedWriter(
                            LOG_FILE,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING,
                            StandardOpenOption.WRITE)) {

                while (true) {

                    // Wait for messages without holding
                    // the LoggerProcess object lock.
                    LogEntry entry = logQueue.take();

                    if (entry.stop) {
                        break;
                    }

                    writer.write(entry.message);
                    writer.newLine();

                    // Immediately save the message
                    writer.flush();

                    System.out.println(entry.message);
                }
            }

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

        } catch (IOException e) {

            System.err.println(
                    "Logger error: " + e.getMessage()
            );

        } finally {

            System.out.println("Logger thread stopped");
        }
    }

    // ==========================================
    // SHUTDOWN
    // ==========================================

    public synchronized void shutdown() {

        if (accepting) {

            accepting = false;

            // Stop marker is added after pending messages.
            logQueue.offer(
                    new LogEntry("", true)
            );
        }
    }

    // ==========================================
    // WAIT FOR LOGGER
    // ==========================================

    public void awaitTermination()
            throws InterruptedException {

        Thread thread = loggerThread;

        if (thread != null) {
            thread.join();
        }
    }

    // ==========================================
    // LOGGER TEST
    // ==========================================

    public static void main(String[] args) {

        LoggerProcess logger = new LoggerProcess();

        logger.start();

        logger.info("Logger started.");
        logger.info("Simulator UI is ready.");
        logger.warning("Program has not been loaded.");
        logger.error("Test error message.");

        logger.shutdown();

        try {

            logger.awaitTermination();

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
        }

        System.out.println("Logging test completed.");
    }
}