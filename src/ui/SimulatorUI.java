
package src.ui;
import src.ipc.IPCChannel;
import src.ipc.IPCMessage;
import src.ipc.CoreProcess;
import src.cpu.CPU;
import src.memory.Memory;
import src.queue.FIFOQueue;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import src.log.LoggerProcess;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;


public class SimulatorUI extends JFrame {

    private CPU cpu;
    private IPCChannel channel;
    private CoreProcess coreProcess;
    private LoggerProcess logger;

    // One background worker handles one Core command at a time.
    private SwingWorker<String, Void> commandWorker;
    private SwingWorker<String, Void> startupWorker;
    private SwingWorker<Integer, String> runWorker;

    // Keep file logging away from Swing's Event Dispatch Thread.
    private final ExecutorService uiLogExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "UI-Log-Dispatch");
        t.setDaemon(true);
        return t;
    });

    //private UICommunicator communicator = new UICommunicator();


    // Program
    private JTextArea programArea;

    // CPU State
    private JLabel currentInstructionLabel;
    private JLabel pcLabel;
    private JLabel accumulatorLabel;
    private JLabel bLabel;
    private JLabel r0Label;
    private JLabel r1Label;
    private JLabel carryFlagLabel;
    private JLabel zeroFlagLabel;
    private JLabel statusLabel;
    private JLabel spLabel;

    // Queue
    private JTextArea queueArea;
    private JLabel frontLabel;
    private JLabel rearLabel;
    private JLabel sizeLabel;
    private JLabel queueStatusLabel;

    // Stack, Memory and Logs
    private JTextArea stackArea;
    private JTextArea memoryArea;
    private JTextArea traceArea;
    private JTextArea changesArea;

    // Control buttons
    private JButton loadButton;
    private JButton resetButton;
    private JButton stepButton;
    private JButton runButton;

    private boolean programLoaded = false;
    private boolean coreReady = false;
    private int traceNumber = 0;

    // Previous values for detecting changes
    private int oldACC;
    private int oldB;
    private int oldR0;
    private int oldR1;
    private boolean oldCarry;
    private int oldSP;
    private int oldQueueSize;

    public SimulatorUI() {

        // Create shared objects first. Do not start worker threads yet.
        cpu = new CPU();
        logger = new LoggerProcess();
        

// Start Logger thread
logger.start();
        channel = new IPCChannel();
        // Start Core with the same CPU and Logger
        coreProcess = CoreProcess.startCore(
       channel,
       logger,
        cpu
);

        setTitle(
            "Nuvoton MS51FB9AE Educational Microcontroller Simulator"
        );
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 780);
        setMinimumSize(new Dimension(1000, 700));
        setLocationRelativeTo(null);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                try {
                    if (coreReady && channel != null) {
                        channel.sendToCore(new IPCMessage("STOP", null));
                    }
                } catch (RuntimeException shutdownError) {
                    // Only RuntimeException is caught here because IPCChannel may throw unchecked exceptions.
                    // If checked exceptions (e.g., IOException) are possible, add another catch block.
                    System.err.println("[SHUTDOWN] Core stop request failed: "
                            + shutdownError.getMessage());
                    JOptionPane.showMessageDialog(
                        SimulatorUI.this,
                        "Failed to properly shut down the Core process.\n" +
                        "Some resources may not have been released correctly.\n\n" +
                        "Error: " + shutdownError.getMessage(),
                        "Shutdown Warning",
                        JOptionPane.WARNING_MESSAGE
                    );
                } finally {
                    // Ensure resources are cleaned up
                    uiLogExecutor.shutdown();
                }
                uiLogExecutor.shutdown();
            }
        });

        // Build and display the complete Swing interface before starting Core.
        createUI();
        loadDefaultProgram();
        savePreviousState();
        updateAllDisplays();

        // Keep controls disabled until Logger and Core are ready.
        setRunControlsEnabled(false);
        statusLabel.setText("STARTING...");
        loadButton.setText("STARTING...");

        setVisible(true);

        // Give Swing time to paint the window. The timer callback only starts
        // a SwingWorker; logger/core startup therefore does not block the EDT.
        Timer startupTimer = new Timer(200, e -> startBackgroundServices());
        startupTimer.setRepeats(false);
        startupTimer.start();
    }

    /**
     * Start Logger and Core off the Swing Event Dispatch Thread, then verify
     * that the Core is actually receiving and answering IPC messages.
     */
    private void startBackgroundServices() {
        if (startupWorker != null && !startupWorker.isDone()) {
            return;
        }

        startupWorker = new SwingWorker<String, Void>() {

            @Override
            protected String doInBackground() throws Exception {
                System.out.println("[STARTUP] Starting logger...");
                logger.start();
                logAsync("INFO", "Logger started");

                System.out.println("[STARTUP] Starting Core thread...");
                coreProcess = CoreProcess.startCore(channel, logger, cpu);

                // Verify the actual UI -> Core -> UI path before enabling controls.
                System.out.println("[STARTUP] Sending PING to Core...");
                channel.sendToCore(new IPCMessage("PING", "Startup handshake"));

                final long deadline = System.nanoTime() + 5_000_000_000L;
                while (System.nanoTime() < deadline) {
                    IPCMessage response = channel.receiveFromCore();

                    if (response == null) {
                        Thread.sleep(10);
                        continue;
                    }

                    String responseCommand = response.getCommand();
                    String responseData = response.getData();

                    if ("ERROR".equalsIgnoreCase(responseCommand)) {
                        throw new IllegalStateException(
                                responseData == null ? "Core returned an error" : responseData);
                    }

                    if ("RESPONSE".equalsIgnoreCase(responseCommand)
                            && "PONG".equalsIgnoreCase(responseData)) {
                        System.out.println("[STARTUP] Core IPC handshake successful.");
                        return "PONG";
                    }

                    logAsync("WARNING", "Unexpected startup response: "
                            + responseCommand + " / " + responseData);
                }

                throw new java.util.concurrent.TimeoutException(
                        "Core did not answer the startup PING within 5 seconds. "
                        + "Check IPCChannel and CoreProcess.");
            }

            @Override
            protected void done() {
                // SwingWorker.done() runs on the EDT. Enable controls first,
                // before any logging or display work that could fail.
                System.out.println("[STARTUP] UI completion callback entered on "
                        + Thread.currentThread().getName());

                try {
                    String result = get();
                    if (!"PONG".equalsIgnoreCase(result)) {
                        throw new IllegalStateException(
                                "Unexpected Core handshake response: " + result);
                    }

                    coreReady = true;
                    programLoaded = false;

                    // These are the most important startup updates.
                    loadButton.setText("LOAD");
                    statusLabel.setText("READY - LOAD PROGRAM");
                    setRunControlsEnabled(true);

                    System.out.println("[STARTUP] Core is READY; buttons enabled.");

                    // Refresh after enabling controls so a display issue cannot
                    // leave all buttons disabled.
                    try {
                        updateAllDisplays();
                        statusLabel.setText("READY - LOAD PROGRAM");
                    } catch (RuntimeException displayError) {
                        System.err.println("[STARTUP] Initial display refresh failed: "
                                + displayError);
                        displayError.printStackTrace();
                    }

                    addTrace("Core communication established.");
                    logAsync("INFO", "Simulator UI and Core are ready");

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    showStartupError("Startup was interrupted.");

                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    String message = (cause != null && cause.getMessage() != null)
                            ? cause.getMessage()
                            : (cause == null ? e.toString() : cause.toString());
                    showStartupError(message);

                } catch (RuntimeException e) {
                    showStartupError(e.getMessage() == null ? e.toString() : e.getMessage());
                }
            }
        };

        startupWorker.execute();
    }

    /** Submit log writes away from Swing's EDT so a slow logger cannot freeze the UI. */
    private void logAsync(String level, String message) {
        try {
            uiLogExecutor.execute(() -> {
                try {
                    if (logger == null) {
                        return;
                    }

                    switch (level == null ? "INFO" : level.toUpperCase()) {
                        case "WARNING":
                            logger.warning(message);
                            break;
                        case "ERROR":
                            logger.error(message);
                            break;
                        default:
                            logger.info(message);
                            break;
                    }
                } catch (RuntimeException loggingError) {
                    System.err.println("[UI LOGGER] " + loggingError.getMessage());
                }
            });
        } catch (RejectedExecutionException ignored) {
            // The window is closing; do not interrupt Swing event handling.
        }
    }

    private void showStartupError(String message) {
        coreReady = false;
        setRunControlsEnabled(false);
        loadButton.setText("START FAILED");
        statusLabel.setText("CORE CONNECTION FAILED");
        System.err.println("[STARTUP ERROR] " + message);
        JOptionPane.showMessageDialog(
                SimulatorUI.this,
                "The simulator could not connect to Core.\n\n" + message,
                "Startup Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    // =====================================================
    // MAIN UI
    // =====================================================

    private void createUI() {

        setLayout(new BorderLayout(8, 8));
        getContentPane().setBackground(
            new Color(242, 245, 249)
        );

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(30, 90, 150));
        titlePanel.setBorder(
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        );

        JLabel title = new JLabel(
            "Nuvoton MS51FB9AE Educational Microcontroller Simulator",
            SwingConstants.CENTER
        );

        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));

        titlePanel.add(title, BorderLayout.CENTER);
        add(titlePanel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(242, 245, 249));
        mainPanel.setBorder(
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        );

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.BOTH;

        // Row 1: Program and CPU State
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.45;
        gbc.weighty = 0.30;
        mainPanel.add(createProgramPanel(), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.55;
        mainPanel.add(createCPUStatePanel(), gbc);

        // Row 2: Queue, Stack and Memory
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.23;
        gbc.weighty = 0.35;
        mainPanel.add(createQueuePanel(), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.32;
        mainPanel.add(createStackPanel(), gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.45;
        mainPanel.add(createMemoryPanel(), gbc);

        // Row 3: Execution Trace and Changes
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 0.50;
        gbc.weighty = 0.35;
        mainPanel.add(createTracePanel(), gbc);

        gbc.gridx = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0.50;
        mainPanel.add(createChangesPanel(), gbc);

        add(mainPanel, BorderLayout.CENTER);
    }

    // =====================================================
    // PROGRAM MEMORY
    // =====================================================

    private JPanel createProgramPanel() {

        JPanel panel = createPanel("Program Memory (Editable)");
        panel.setLayout(new BorderLayout(5, 5));

        programArea = new JTextArea();
        programArea.setFont(
            new Font("Consolas", Font.PLAIN, 14)
        );
        programArea.setLineWrap(false);
        programArea.setEditable(true);
        programArea.setBorder(
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        );

        panel.add(
            new JScrollPane(programArea),
            BorderLayout.CENTER
        );

        return panel;
    }

    // =====================================================
    // CPU STATE AND BUTTONS
    // =====================================================

    private JPanel createCPUStatePanel() {

        JPanel outerPanel = createPanel("CPU State");
        outerPanel.setLayout(new BorderLayout(5, 5));

        JPanel stateGrid = new JPanel(
            new GridLayout(5, 4, 8, 6)
        );
        stateGrid.setBackground(Color.WHITE);

        currentInstructionLabel = createValueLabel("NONE");
        pcLabel = createValueLabel("0000");
        accumulatorLabel = createValueLabel("00");
        bLabel = createValueLabel("00");
        r0Label = createValueLabel("00");
        r1Label = createValueLabel("00");
        carryFlagLabel = createValueLabel("0");
        zeroFlagLabel = createValueLabel("N/A");
        statusLabel = createValueLabel("RESET");
        spLabel = createValueLabel("07");

        addStateItem(stateGrid, "Current Instruction:",
                currentInstructionLabel);
        addStateItem(stateGrid, "Program Counter (PC):",
                pcLabel);
        addStateItem(stateGrid, "Accumulator (ACC):",
                accumulatorLabel);
        addStateItem(stateGrid, "B Register:",
                bLabel);
        addStateItem(stateGrid, "Register R0:",
                r0Label);
        addStateItem(stateGrid, "Register R1:",
                r1Label);
        addStateItem(stateGrid, "Carry Flag (CY):",
                carryFlagLabel);
        addStateItem(stateGrid, "Zero Flag:",
                zeroFlagLabel);
        addStateItem(stateGrid, "Execution Status:",
                statusLabel);
        addStateItem(stateGrid, "Stack Pointer (SP):",
                spLabel);

        outerPanel.add(stateGrid, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(
            new GridLayout(1, 4, 8, 0)
        );
        buttonPanel.setBackground(Color.WHITE);

        // Create buttons
        loadButton = createButton(
            "LOAD", new Color(38, 150, 90)
        );

        resetButton = createButton(
            "RESET", new Color(90, 120, 150)
        );

        stepButton = createButton(
            "STEP", new Color(30, 110, 210)
        );

        runButton = createButton(
            "RUN", new Color(110, 70, 190)
        );

        // Connect buttons to their methods
        loadButton.addActionListener(this::loadProgram);
        resetButton.addActionListener(this::resetCPU);
        stepButton.addActionListener(this::stepCPU);
        runButton.addActionListener(this::runCPU);

        buttonPanel.add(loadButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(stepButton);
        buttonPanel.add(runButton);

        outerPanel.add(buttonPanel, BorderLayout.SOUTH);

        return outerPanel;
    }

    // =====================================================
    // FIFO QUEUE PANEL
    // =====================================================

    private JPanel createQueuePanel() {

        JPanel panel = createPanel("FIFO Queue");
        panel.setLayout(new BorderLayout(5, 5));

        queueArea = new JTextArea();
        queueArea.setEditable(false);
        queueArea.setFont(
            new Font("Consolas", Font.PLAIN, 13)
        );
        queueArea.setLineWrap(true);
        queueArea.setWrapStyleWord(true);

        panel.add(
            new JScrollPane(queueArea),
            BorderLayout.CENTER
        );

        JPanel infoPanel = new JPanel(
            new GridLayout(4, 2, 5, 3)
        );
        infoPanel.setBackground(Color.WHITE);

        frontLabel = createSmallValueLabel("0");
        rearLabel = createSmallValueLabel("0");
        sizeLabel = createSmallValueLabel("0 / 8");
        queueStatusLabel = createSmallValueLabel("EMPTY");

        addSmallItem(infoPanel, "Front:", frontLabel);
        addSmallItem(infoPanel, "Rear:", rearLabel);
        addSmallItem(infoPanel, "Size:", sizeLabel);
        addSmallItem(infoPanel, "Status:", queueStatusLabel);

        panel.add(infoPanel, BorderLayout.SOUTH);

        return panel;
    }

    // =====================================================
    // STACK PANEL
    // =====================================================

    private JPanel createStackPanel() {

        JPanel panel = createPanel("Stack");
        panel.setLayout(new BorderLayout(5, 5));

        stackArea = new JTextArea();
        stackArea.setEditable(false);
        stackArea.setFont(
            new Font("Consolas", Font.PLAIN, 13)
        );

        panel.add(
            new JScrollPane(stackArea),
            BorderLayout.CENTER
        );

        return panel;
    }

    // =====================================================
    // DATA MEMORY PANEL
    // =====================================================

    private JPanel createMemoryPanel() {

        JPanel panel = createPanel("Data Memory");
        panel.setLayout(new BorderLayout());

        memoryArea = new JTextArea();
        memoryArea.setEditable(false);
        memoryArea.setFont(
            new Font("Consolas", Font.PLAIN, 12)
        );

        panel.add(
            new JScrollPane(memoryArea),
            BorderLayout.CENTER
        );

        return panel;
    }

    // =====================================================
    // EXECUTION TRACE PANEL
    // =====================================================

    private JPanel createTracePanel() {

        JPanel panel = createPanel("Execution Trace");
        panel.setLayout(new BorderLayout());

        traceArea = new JTextArea();
        traceArea.setEditable(false);
        traceArea.setFont(
            new Font("Consolas", Font.PLAIN, 12)
        );

        panel.add(
            new JScrollPane(traceArea),
            BorderLayout.CENTER
        );

        return panel;
    }

    // =====================================================
    // CHANGES PANEL
    // =====================================================

    private JPanel createChangesPanel() {

        JPanel panel = createPanel(
            "Register / Memory / Queue Changes"
        );
        panel.setLayout(new BorderLayout());

        changesArea = new JTextArea();
        changesArea.setEditable(false);
        changesArea.setFont(
            new Font("Consolas", Font.PLAIN, 12)
        );

        panel.add(
            new JScrollPane(changesArea),
            BorderLayout.CENTER
        );

        return panel;
    }

    // =====================================================
    // HELPER METHODS FOR UI DESIGN
    // =====================================================

    private JPanel createPanel(String title) {

        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);

        panel.setBorder(
            BorderFactory.createCompoundBorder(
                new TitledBorder(
                    new LineBorder(new Color(150, 180, 210), 1),
                    title,
                    TitledBorder.LEFT,
                    TitledBorder.TOP,
                    new Font("Segoe UI", Font.BOLD, 14),
                    new Color(30, 70, 110)
                ),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)
            )
        );

        return panel;
    }

    private void addStateItem(
            JPanel panel, String name, JLabel value) {

        JLabel label = new JLabel(name);
        label.setFont(
            new Font("Segoe UI", Font.BOLD, 12)
        );

        panel.add(label);
        panel.add(value);
    }

    private void addSmallItem(
            JPanel panel, String name, JLabel value) {

        JLabel label = new JLabel(name);
        label.setFont(
            new Font("Segoe UI", Font.BOLD, 12)
        );

        panel.add(label);
        panel.add(value);
    }

    private JLabel createValueLabel(String text) {

        JLabel label = new JLabel(text);
        label.setOpaque(true);
        label.setBackground(new Color(245, 248, 252));
        label.setBorder(
            BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 210, 220)),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)
            )
        );
        label.setFont(
            new Font("Consolas", Font.BOLD, 13)
        );

        return label;
    }

    private JLabel createSmallValueLabel(String text) {

        JLabel label = new JLabel(text, SwingConstants.LEFT);
        label.setFont(
            new Font("Consolas", Font.BOLD, 12)
        );

        return label;
    }

    private JButton createButton(String text, Color color) {

        JButton button = new JButton(text);
        button.setFont(
            new Font("Segoe UI", Font.BOLD, 12)
        );
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorder(
            BorderFactory.createEmptyBorder(7, 10, 7, 10)
        );

        return button;
    }

    // =====================================================
    // DEFAULT PROGRAM
    // =====================================================

    private void loadDefaultProgram() {

        programArea.setText(
            "ENQ #10\n" +
            "ENQ #20\n" +
            "ENQ #30\n" +
            "DEQ\n" +
            "DEQ\n" +
            "HALT"
        );
    }

    // =====================================================
    // THREAD CONTROL AND IPC HELPERS
    // =====================================================

    private boolean isRunning() {
        return commandWorker != null && !commandWorker.isDone();
    }

    private void setRunControlsEnabled(boolean enabled) {
        loadButton.setEnabled(enabled);
        resetButton.setEnabled(enabled);
        stepButton.setEnabled(enabled);
        runButton.setEnabled(enabled);
        programArea.setEditable(enabled);
    }

    /**
     * Send one command to Core and wait for exactly one response.
     * This method is called only by SwingWorker.doInBackground(), never by the EDT.
     */
    private String sendCommandAndWait(String command, String data) throws Exception {
        channel.sendToCore(new IPCMessage(command, data));

        final long deadline = System.nanoTime() + 5_000_000_000L;

        while (System.nanoTime() < deadline) {
            IPCMessage response = channel.receiveFromCore();

            if (response != null) {
                String responseCommand = response.getCommand();
                String responseData = response.getData();

                if ("ERROR".equalsIgnoreCase(responseCommand)) {
                    throw new IllegalStateException(responseData);
                }

                if ("RESPONSE".equalsIgnoreCase(responseCommand)) {
                    return responseData;
                }

                // Ignore any non-response messages rather than treating them as success.
                logAsync("WARNING", "Ignoring unexpected Core message: " + responseCommand);
            }

            Thread.sleep(10);
        }

        throw new java.util.concurrent.TimeoutException(
                "Timed out waiting for Core response to " + command
        );
    }

    /**
     * Executes a Core command off the Swing Event Dispatch Thread.
     * UI callbacks run on the EDT after the response is received.
     */
    private void executeCoreCommand(
            String command,
            String data,
            Consumer<String> onSuccess) {

        if (!coreReady) {
            JOptionPane.showMessageDialog(
                this,
                "Core is still starting. Please try again in a moment.",
                "Core Not Ready",
                JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        if (isRunning()) {
            return;
        }

        setRunControlsEnabled(false);
        statusLabel.setText(command + "...");
        logAsync("INFO", "UI sending command: " + command);

        commandWorker = new SwingWorker<String, Void>() {

            @Override
            protected String doInBackground() throws Exception {
                return sendCommandAndWait(command, data);
            }

            @Override
            protected void done() {
                try {
                    String response = get();
                    logAsync("INFO", "Core response for " + command + ": " + response);

                    if (onSuccess != null) {
                        onSuccess.accept(response);
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    showCommandError(command, "Operation interrupted.");

                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    String message = (cause != null && cause.getMessage() != null)
                            ? cause.getMessage()
                            : (cause == null ? e.toString() : cause.toString());
                    showCommandError(command, message);

                } finally {
                    try {
                        // Core and UI refer to the same CPU; refresh after Core finishes.
                        updateAllDisplays();
                    } catch (RuntimeException displayError) {
                        logAsync("ERROR", "Display refresh failed: " + displayError.getMessage());
                        displayError.printStackTrace();
                    } finally {
                        setRunControlsEnabled(coreReady);
                        loadButton.setText("LOAD");
                        statusLabel.setText(
                                cpu.isHalted() ? "HALTED" :
                                (programLoaded ? "RUNNING / READY" :
                                 (coreReady ? "READY - LOAD PROGRAM" : "CORE NOT READY"))
                        );
                        commandWorker = null;
                    }
                }
            }
        };

        commandWorker.execute();
    }

    private void showCommandError(String command, String message) {
        logAsync("ERROR", command + " failed: " + message);
        addTrace(command + " ERROR: " + message);
        JOptionPane.showMessageDialog(
                this,
                message,
                command + " Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    // =====================================================
    // LOAD BUTTON
    // =====================================================

    private void loadProgram(ActionEvent event) {
        if (isRunning()) {
            return;
        }

        final String programText = programArea.getText();
        if (programText == null || programText.trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a program first.",
                    "Empty Program",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        programLoaded = false;
        loadButton.setText("LOADING...");
        executeCoreCommand("LOAD", programText, response -> {
            try {
            programLoaded = true;

            logger.info("UI: LOAD command completed successfully.");



            traceArea.setText("");
            changesArea.setText("");
            traceNumber = 0;
            addTrace(response);
            addTrace("Ready for STEP or RUN.");
            savePreviousState();
            updateAllDisplays();

                } catch (Exception e) {
            logger.error("UI: LOAD failed - " + e.getMessage());

            JOptionPane.showMessageDialog(
                this,
                e.getMessage(),
                "Program Load Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    });
}

    // =====================================================
    // RESET BUTTON
    // =====================================================

    private void resetCPU(ActionEvent event) {
        if (isRunning()) {
            return;
        }

        cpu.reset();
        programLoaded = false;

        traceArea.setText("");
        changesArea.setText("");
        traceNumber = 0;

        savePreviousState();
        updateAllDisplays();

        addTrace("CPU RESET completed.");
        logger.info("UI: RESET command completed successfully.");
    }
    // =====================================================
// STEP BUTTON
// =====================================================

private void stepCPU(ActionEvent event) {

    if (isRunning()) {
        return;
    }

    if (!programLoaded) {
        JOptionPane.showMessageDialog(
            this,
            "Please LOAD the program first.",
            "Program Not Loaded",
            JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    if (cpu.isHalted()) {
        addTrace("CPU is already HALTED.");
        return;
    }

    executeCoreCommand("STEP", null, response -> {
        addTrace(response);
        showChanges();
    });
}
    // =====================================================
    // RUN BUTTON
    // =====================================================

    private void runCPU(ActionEvent event) {
        if (isRunning()) {
            return;
        }

        if (!programLoaded) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please LOAD the program first.",
                    "Program Not Loaded",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (cpu.isHalted()) {
            addTrace("CPU is already HALTED.");
            return;
        }

        executeCoreCommand("RUN", null, response -> {
            addTrace(response);
            showChanges();
            logAsync("INFO", "RUN command completed");
        });
    }

    // =====================================================
    // UPDATE ALL DISPLAYS
    // =====================================================

    private void updateAllDisplays() {

        updateCPUDisplay();
        updateQueueDisplay();
        updateStackDisplay();
        updateMemoryDisplay();
    }

    // =====================================================
    // CPU DISPLAY
    // =====================================================

    private void updateCPUDisplay() {

        if (cpu.getCurrentInstruction() == null) {
            currentInstructionLabel.setText("NONE");
        } else {
            currentInstructionLabel.setText(
                cpu.getCurrentInstruction().toString()
            );
        }

        pcLabel.setText(String.format(
            "%04X", cpu.getRegisters().getPC()
        ));

        accumulatorLabel.setText(String.format(
            "%02X", cpu.getRegisters().getACC()
        ));

        bLabel.setText(String.format(
            "%02X", cpu.getRegisters().getB()
        ));

        r0Label.setText(String.format(
            "%02X", cpu.getRegisters().getR(0)
        ));

        r1Label.setText(String.format(
            "%02X", cpu.getRegisters().getR(1)
        ));

        carryFlagLabel.setText(
            cpu.getFlags().isCarry() ? "1" : "0"
        );

        statusLabel.setText(
            cpu.isHalted() ? "HALTED"
                : programLoaded ? "RUNNING / READY"
                : coreReady ? "READY - LOAD PROGRAM"
                : "RESET"
        );

        spLabel.setText(String.format(
            "%02X", cpu.getStackPointer().getValue()
        ));

        zeroFlagLabel.setText("N/A");
    }

    // =====================================================
    // FIFO QUEUE DISPLAY
    // =====================================================

    private void updateQueueDisplay() {

        FIFOQueue queue = cpu.getQueue();
        StringBuilder text = new StringBuilder();

        text.append("QUEUE\n");
        text.append("----------------------\n");

        if (queue.isEmpty()) {

            text.append("[ EMPTY ]\n");

        } else {

            text.append("[ ");

            for (int i = 0; i < queue.size(); i++) {

                int index =
                    (queue.getFront() + i)
                    % queue.getCapacity();

                text.append(String.format(
                    "%02X", queue.getValue(index)
                ));

                if (i < queue.size() - 1) {
                    text.append(" | ");
                }
            }

            text.append(" ]\n");
        }

        text.append("\nCapacity : ")
            .append(queue.getCapacity());

        queueArea.setText(text.toString());

        frontLabel.setText(
            String.valueOf(queue.getFront())
        );

        rearLabel.setText(
            String.valueOf(queue.getRear())
        );

        sizeLabel.setText(
            queue.size() + " / " + queue.getCapacity()
        );

        queueStatusLabel.setText(queue.getStatus());

        if (queue.isFull()) {

            queueStatusLabel.setForeground(Color.RED);

        } else if (queue.isEmpty()) {

            queueStatusLabel.setForeground(Color.GRAY);

        } else {

            queueStatusLabel.setForeground(
                new Color(20, 140, 70)
            );
        }
    }

    // =====================================================
    // STACK DISPLAY
    // =====================================================

    private void updateStackDisplay() {

        Memory memory = cpu.getDataMemory();
        int sp = cpu.getStackPointer().getValue();

        StringBuilder text = new StringBuilder();

        text.append(String.format(
            "Stack Pointer (SP): %02X\n\n", sp
        ));

        text.append("Address    Value\n");
        text.append("----------------\n");

        for (int i = 0; i < 12; i++) {

            int address = sp - i;

            if (address < 0) {
                break;
            }

            text.append(String.format(
                "   %02X       %02X\n",
                address,
                memory.read(address)
            ));
        }

        text.append("\nPUSH / POP use Data Memory.");

        stackArea.setText(text.toString());
    }

    // =====================================================
    // DATA MEMORY DISPLAY
    // =====================================================

    private void updateMemoryDisplay() {

        Memory memory = cpu.getDataMemory();
        StringBuilder text = new StringBuilder();

        text.append(
            "Addr       Value       Addr       Value\n"
        );
        text.append(
            "----------------------------------------\n"
        );

        for (int i = 0; i < 128; i++) {

            int second = i + 128;

            text.append(String.format(
                "%02X         %02X",
                i,
                memory.read(i)
            ));

            text.append(String.format(
                "          %02X         %02X",
                second,
                memory.read(second)
            ));

            text.append("\n");
        }

        memoryArea.setText(text.toString());
    }

    // =====================================================
    // EXECUTION TRACE
    // =====================================================

    private void addTrace(String message) {

        traceArea.append(String.format(
            "[%03d] %s\n",
            traceNumber++,
            message
        ));

        traceArea.setCaretPosition(
            traceArea.getDocument().getLength()
        );
    }

    // =====================================================
    // SAVE PREVIOUS CPU STATE
    // =====================================================

    private void savePreviousState() {

        oldACC = cpu.getRegisters().getACC();
        oldB = cpu.getRegisters().getB();
        oldR0 = cpu.getRegisters().getR(0);
        oldR1 = cpu.getRegisters().getR(1);
        oldCarry = cpu.getFlags().isCarry();
        oldSP = cpu.getStackPointer().getValue();
        oldQueueSize = cpu.getQueue().size();
    }

    // =====================================================
    // DETECT REGISTER AND QUEUE CHANGES
    // =====================================================

    private void showChanges() {

        int newACC = cpu.getRegisters().getACC();
        int newB = cpu.getRegisters().getB();
        int newR0 = cpu.getRegisters().getR(0);
        int newR1 = cpu.getRegisters().getR(1);
        boolean newCarry = cpu.getFlags().isCarry();
        int newSP = cpu.getStackPointer().getValue();
        int newQueueSize = cpu.getQueue().size();

        StringBuilder changes = new StringBuilder();

        changes.append("Instruction: ");

        if (cpu.getCurrentInstruction() != null) {

            changes.append(
                cpu.getCurrentInstruction().toString()
            );

        } else {

            changes.append("NONE");
        }

        changes.append("\n");
        changes.append(
            "----------------------------------\n"
        );

        if (newACC != oldACC) {
            changes.append(String.format(
                "ACC: %02X -> %02X\n", oldACC, newACC
            ));
        }

        if (newB != oldB) {
            changes.append(String.format(
                "B:   %02X -> %02X\n", oldB, newB
            ));
        }

        if (newR0 != oldR0) {
            changes.append(String.format(
                "R0:  %02X -> %02X\n", oldR0, newR0
            ));
        }

        if (newR1 != oldR1) {
            changes.append(String.format(
                "R1:  %02X -> %02X\n", oldR1, newR1
            ));
        }

        if (newCarry != oldCarry) {
            changes.append(String.format(
                "CY:  %d -> %d\n",
                oldCarry ? 1 : 0,
                newCarry ? 1 : 0
            ));
        }

        if (newSP != oldSP) {
            changes.append(String.format(
                "SP:  %02X -> %02X\n", oldSP, newSP
            ));
        }

        if (newQueueSize != oldQueueSize) {
            changes.append(String.format(
                "Queue Size: %d -> %d\n",
                oldQueueSize, newQueueSize
            ));
        }

        if (changes.toString().endsWith(
            "----------------------------------\n"
        )) {
            changes.append("No register/queue changes.");
        }

        changesArea.append(changes.toString());
        changesArea.append("\n\n");

        changesArea.setCaretPosition(
            changesArea.getDocument().getLength()
        );

        savePreviousState();
    }

    // =====================================================
    // MAIN METHOD
    // =====================================================

    public static void main(String[] args) {

        // Start the Swing UI on the Event Dispatch Thread
        SwingUtilities.invokeLater(SimulatorUI::new);
    }
}
