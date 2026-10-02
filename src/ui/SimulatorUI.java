package src.ui;

import src.cpu.CPU;
import src.memory.Memory;
import src.queue.FIFOQueue;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class SimulatorUI extends JFrame {

    private CPU cpu;

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

    // Stack
    private JTextArea stackArea;

    // Memory
    private JTextArea memoryArea;

    // Logs
    private JTextArea traceArea;
    private JTextArea changesArea;

    private boolean programLoaded = false;
    private int traceNumber = 0;

    // Previous state for change detection
    private int oldACC;
    private int oldB;
    private int oldR0;
    private int oldR1;
    private boolean oldCarry;
    private int oldSP;
    private int oldQueueSize;

    public SimulatorUI() {

        cpu = new CPU();

        setTitle("Nuvoton MS51FB9AE Educational Microcontroller Simulator");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setSize(1200, 780);
        setMinimumSize(new Dimension(1000, 700));

        setLocationRelativeTo(null);

        createUI();

        loadDefaultProgram();

        updateAllDisplays();

        setVisible(true);
    }

    // =========================================================
    // MAIN UI
    // =========================================================

    private void createUI() {

        setLayout(new BorderLayout(8, 8));

        getContentPane().setBackground(new Color(242, 245, 249));

        // -----------------------------------------------------
        // TITLE
        // -----------------------------------------------------

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

        title.setFont(
                new Font("Segoe UI", Font.BOLD, 22)
        );

        titlePanel.add(title, BorderLayout.CENTER);

        add(titlePanel, BorderLayout.NORTH);

        // -----------------------------------------------------
        // MAIN CONTENT
        // -----------------------------------------------------

        JPanel mainPanel = new JPanel(new GridBagLayout());

        mainPanel.setBackground(new Color(242, 245, 249));

        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        );

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(4, 4, 4, 4);

        gbc.fill = GridBagConstraints.BOTH;

        // =====================================================
        // ROW 1
        // Program Memory
        // CPU State
        // =====================================================

        JPanel programPanel = createProgramPanel();

        gbc.gridx = 0;
        gbc.gridy = 0;

        gbc.weightx = 0.45;
        gbc.weighty = 0.30;

        mainPanel.add(programPanel, gbc);

        JPanel cpuPanel = createCPUStatePanel();

        gbc.gridx = 1;
        gbc.gridy = 0;

        gbc.weightx = 0.55;
        gbc.weighty = 0.30;

        mainPanel.add(cpuPanel, gbc);

        // =====================================================
        // ROW 2
        // FIFO / Stack / Memory
        // =====================================================

        JPanel queuePanel = createQueuePanel();

        gbc.gridx = 0;
        gbc.gridy = 1;

        gbc.weightx = 0.23;
        gbc.weighty = 0.35;

        mainPanel.add(queuePanel, gbc);

        JPanel stackPanel = createStackPanel();

        gbc.gridx = 1;
        gbc.gridy = 1;

        gbc.weightx = 0.32;
        gbc.weighty = 0.35;

        mainPanel.add(stackPanel, gbc);

        JPanel memoryPanel = createMemoryPanel();

        gbc.gridx = 2;
        gbc.gridy = 1;

        gbc.weightx = 0.45;
        gbc.weighty = 0.35;

        mainPanel.add(memoryPanel, gbc);

        // =====================================================
        // ROW 3
        // Execution Trace
        // Changes
        // =====================================================

        JPanel tracePanel = createTracePanel();

        gbc.gridx = 0;
        gbc.gridy = 2;

        gbc.gridwidth = 2;

        gbc.weightx = 0.50;
        gbc.weighty = 0.35;

        mainPanel.add(tracePanel, gbc);

        JPanel changesPanel = createChangesPanel();

        gbc.gridx = 2;
        gbc.gridy = 2;

        gbc.gridwidth = 1;

        gbc.weightx = 0.50;
        gbc.weighty = 0.35;

        mainPanel.add(changesPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);
    }

    // =========================================================
    // PROGRAM MEMORY
    // =========================================================

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

        JScrollPane scrollPane = new JScrollPane(programArea);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // =========================================================
    // CPU STATE
    // =========================================================

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

        addStateItem(
                stateGrid,
                "Current Instruction:",
                currentInstructionLabel
        );

        addStateItem(
                stateGrid,
                "Program Counter (PC):",
                pcLabel
        );

        addStateItem(
                stateGrid,
                "Accumulator (ACC):",
                accumulatorLabel
        );

        addStateItem(
                stateGrid,
                "B Register:",
                bLabel
        );

        addStateItem(
                stateGrid,
                "Register R0:",
                r0Label
        );

        addStateItem(
                stateGrid,
                "Register R1:",
                r1Label
        );

        addStateItem(
                stateGrid,
                "Carry Flag (CY):",
                carryFlagLabel
        );

        addStateItem(
                stateGrid,
                "Zero Flag:",
                zeroFlagLabel
        );

        addStateItem(
                stateGrid,
                "Execution Status:",
                statusLabel
        );

        addStateItem(
                stateGrid,
                "Stack Pointer (SP):",
                spLabel
        );

        outerPanel.add(stateGrid, BorderLayout.CENTER);

        // -----------------------------------------------------
        // BUTTONS
        // -----------------------------------------------------

        JPanel buttonPanel = new JPanel(
                new GridLayout(1, 4, 8, 0)
        );

        buttonPanel.setBackground(Color.WHITE);

        JButton loadButton = createButton(
                "LOAD",
                new Color(38, 150, 90)
        );

        JButton resetButton = createButton(
                "RESET",
                new Color(90, 120, 150)
        );

        JButton stepButton = createButton(
                "STEP",
                new Color(30, 110, 210)
        );

        JButton runButton = createButton(
                "RUN",
                new Color(110, 70, 190)
        );

        loadButton.addActionListener(this::loadProgram);

        resetButton.addActionListener(this::resetCPU);

        stepButton.addActionListener(this::stepCPU);

        runButton.addActionListener(this::runCPU);

        buttonPanel.add(loadButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(stepButton);
        buttonPanel.add(runButton);

        outerPanel.add(
                buttonPanel,
                BorderLayout.SOUTH
        );

        return outerPanel;
    }

    // =========================================================
    // FIFO QUEUE
    // =========================================================

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

        queueArea.setBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        );

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

        addSmallItem(
                infoPanel,
                "Front:",
                frontLabel
        );

        addSmallItem(
                infoPanel,
                "Rear:",
                rearLabel
        );

        addSmallItem(
                infoPanel,
                "Size:",
                sizeLabel
        );

        addSmallItem(
                infoPanel,
                "Status:",
                queueStatusLabel
        );

        panel.add(
                infoPanel,
                BorderLayout.SOUTH
        );

        return panel;
    }

    // =========================================================
    // STACK
    // =========================================================

    private JPanel createStackPanel() {

        JPanel panel = createPanel("Stack");

        panel.setLayout(new BorderLayout(5, 5));

        JPanel spPanel = new JPanel(
                new BorderLayout(5, 5)
        );

        spPanel.setBackground(Color.WHITE);

        JLabel spTitle = new JLabel("Stack Pointer (SP):");

        spTitle.setFont(
                new Font("Segoe UI", Font.BOLD, 13)
        );

        JLabel spValue = createSmallValueLabel("07");

        spPanel.add(
                spTitle,
                BorderLayout.WEST
        );

        spPanel.add(
                spValue,
                BorderLayout.CENTER
        );

        stackArea = new JTextArea();

        stackArea.setEditable(false);

        stackArea.setFont(
                new Font("Consolas", Font.PLAIN, 13)
        );

        panel.add(
                spPanel,
                BorderLayout.NORTH
        );

        panel.add(
                new JScrollPane(stackArea),
                BorderLayout.CENTER
        );

        return panel;
    }

    // =========================================================
    // DATA MEMORY
    // =========================================================

    private JPanel createMemoryPanel() {

        JPanel panel = createPanel("Data Memory");

        panel.setLayout(new BorderLayout());

        memoryArea = new JTextArea();

        memoryArea.setEditable(false);

        memoryArea.setFont(
                new Font("Consolas", Font.PLAIN, 12)
        );

        memoryArea.setLineWrap(false);

        panel.add(
                new JScrollPane(memoryArea),
                BorderLayout.CENTER
        );

        return panel;
    }

    // =========================================================
    // EXECUTION TRACE
    // =========================================================

    private JPanel createTracePanel() {

        JPanel panel = createPanel("Execution Trace");

        panel.setLayout(new BorderLayout());

        traceArea = new JTextArea();

        traceArea.setEditable(false);

        traceArea.setFont(
                new Font("Consolas", Font.PLAIN, 12)
        );

        traceArea.setLineWrap(false);

        panel.add(
                new JScrollPane(traceArea),
                BorderLayout.CENTER
        );

        return panel;
    }

    // =========================================================
    // CHANGES
    // =========================================================

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

        changesArea.setLineWrap(false);

        panel.add(
                new JScrollPane(changesArea),
                BorderLayout.CENTER
        );

        return panel;
    }

    // =========================================================
    // PANEL CREATION
    // =========================================================

    private JPanel createPanel(String title) {

        JPanel panel = new JPanel();

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        new TitledBorder(
                                new LineBorder(
                                        new Color(150, 180, 210),
                                        1
                                ),
                                title,
                                TitledBorder.LEFT,
                                TitledBorder.TOP,
                                new Font(
                                        "Segoe UI",
                                        Font.BOLD,
                                        14
                                ),
                                new Color(30, 70, 110)
                        ),
                        BorderFactory.createEmptyBorder(
                                3, 3, 3, 3
                        )
                )
        );

        return panel;
    }

    // =========================================================
    // STATE ITEM
    // =========================================================

    private void addStateItem(
            JPanel panel,
            String name,
            JLabel value
    ) {

        JLabel label = new JLabel(name);

        label.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );

        panel.add(label);

        panel.add(value);
    }

    private void addSmallItem(
            JPanel panel,
            String name,
            JLabel value
    ) {

        JLabel label = new JLabel(name);

        label.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );

        panel.add(label);

        panel.add(value);
    }

    // =========================================================
    // LABEL CREATION
    // =========================================================

    private JLabel createValueLabel(String text) {

        JLabel label = new JLabel(text);

        label.setOpaque(true);

        label.setBackground(
                new Color(245, 248, 252)
        );

        label.setBorder(
                BorderFactory.createCompoundBorder(
                        new LineBorder(
                                new Color(200, 210, 220)
                        ),
                        BorderFactory.createEmptyBorder(
                                4, 6, 4, 6
                        )
                )
        );

        label.setFont(
                new Font(
                        "Consolas",
                        Font.BOLD,
                        13
                )
        );

        return label;
    }

    private JLabel createSmallValueLabel(String text) {

        JLabel label = new JLabel(
                text,
                SwingConstants.LEFT
        );

        label.setFont(
                new Font(
                        "Consolas",
                        Font.BOLD,
                        12
                )
        );

        return label;
    }

    // =========================================================
    // BUTTON
    // =========================================================

    private JButton createButton(
            String text,
            Color color
    ) {

        JButton button = new JButton(text);

        button.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );

        button.setForeground(Color.WHITE);

        button.setBackground(color);

        button.setFocusPainted(false);

        button.setBorder(
                BorderFactory.createEmptyBorder(
                        7, 10, 7, 10
                )
        );

        return button;
    }

    // =========================================================
    // DEFAULT PROGRAM
    // =========================================================

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

    // =========================================================
    // LOAD
    // =========================================================

    private void loadProgram(ActionEvent event) {

        try {

            cpu.reset();

            cpu.loadProgram(
                    programArea.getText()
            );

            programLoaded = true;

            traceArea.setText("");

            changesArea.setText("");

            traceNumber = 0;

            addTrace(
                    "Program loaded successfully."
            );

            addTrace(
                    "Ready for STEP or RUN."
            );

            savePreviousState();

            updateAllDisplays();

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Program Load Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =========================================================
    // RESET
    // =========================================================

    private void resetCPU(ActionEvent event) {

        cpu.reset();

        programLoaded = false;

        traceArea.setText("");

        changesArea.setText("");

        traceNumber = 0;

        updateAllDisplays();

        addTrace(
                "CPU RESET completed."
        );
    }

    // =========================================================
    // STEP
    // =========================================================

    private void stepCPU(ActionEvent event) {

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

            addTrace(
                    "CPU is already HALTED."
            );

            return;
        }

        try {

            int pcBefore =
                    cpu.getRegisters().getPC();

            cpu.fetch();

            String instruction =
                    cpu.getCurrentInstruction().toString();

            cpu.decode();

            String decoded =
                    cpu.getDecodedInstruction().toString();

            cpu.execute();

            addTrace(
                    String.format(
                            "PC %04X | FETCH: %s | DECODE: %s | EXECUTE completed",
                            pcBefore,
                            instruction,
                            decoded
                    )
            );

            showChanges();

            updateAllDisplays();

        } catch (Exception e) {

            addTrace(
                    "ERROR: " + e.getMessage()
            );

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Execution Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =========================================================
    // RUN
    // =========================================================

    private void runCPU(ActionEvent event) {

        if (!programLoaded) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please LOAD the program first.",
                    "Program Not Loaded",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        int count = 0;

        try {

            while (!cpu.isHalted() && count < 100) {

                int pcBefore =
                        cpu.getRegisters().getPC();

                cpu.fetch();

                String instruction =
                        cpu.getCurrentInstruction().toString();

                cpu.decode();

                String decoded =
                        cpu.getDecodedInstruction().toString();

                cpu.execute();

                addTrace(
                        String.format(
                                "PC %04X | %s | %s",
                                pcBefore,
                                instruction,
                                decoded
                        )
                );

                showChanges();

                updateAllDisplays();

                count++;
            }

            if (cpu.isHalted()) {

                addTrace(
                        "Program execution terminated normally."
                );

            } else if (count >= 100) {

                addTrace(
                        "RUN stopped after 100 instructions."
                );
            }

        } catch (Exception e) {

            addTrace(
                    "ERROR: " + e.getMessage()
            );

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Execution Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =========================================================
    // UPDATE ALL DISPLAYS
    // =========================================================

    private void updateAllDisplays() {

        updateCPUDisplay();

        updateQueueDisplay();

        updateStackDisplay();

        updateMemoryDisplay();
    }

    // =========================================================
    // CPU DISPLAY
    // =========================================================

    private void updateCPUDisplay() {

        if (cpu.getCurrentInstruction() == null) {

            currentInstructionLabel.setText(
                    "NONE"
            );

        } else {

            currentInstructionLabel.setText(
                    cpu.getCurrentInstruction().toString()
            );
        }

        pcLabel.setText(
                String.format(
                        "%04X",
                        cpu.getRegisters().getPC()
                )
        );

        accumulatorLabel.setText(
                String.format(
                        "%02X",
                        cpu.getRegisters().getACC()
                )
        );

        bLabel.setText(
                String.format(
                        "%02X",
                        cpu.getRegisters().getB()
                )
        );

        r0Label.setText(
                String.format(
                        "%02X",
                        cpu.getRegisters().getR(0)
                )
        );

        r1Label.setText(
                String.format(
                        "%02X",
                        cpu.getRegisters().getR(1)
                )
        );

        carryFlagLabel.setText(
                cpu.getFlags().isCarry()
                        ? "1"
                        : "0"
        );

        statusLabel.setText(
                cpu.isHalted()
                        ? "HALTED"
                        : programLoaded
                        ? "RUNNING / READY"
                        : "RESET"
        );

        spLabel.setText(
                String.format(
                        "%02X",
                        cpu.getStackPointer().getValue()
                )
        );

        // This simulator does not currently
        // maintain a dedicated Zero flag.
        zeroFlagLabel.setText("N/A");
    }

    // =========================================================
    // QUEUE DISPLAY
    // =========================================================

    private void updateQueueDisplay() {

        FIFOQueue queue =
                cpu.getQueue();

        StringBuilder text =
                new StringBuilder();

        text.append("QUEUE\n");
        text.append("----------------------\n");

        if (queue.isEmpty()) {

            text.append("[ EMPTY ]\n");

        } else {

            text.append("[ ");

            for (int i = 0;
                 i < queue.size();
                 i++) {

                int index =
                        (queue.getFront() + i)
                                % queue.getCapacity();

                text.append(
                        String.format(
                                "%02X",
                                queue.getValue(index)
                        )
                );

                if (i < queue.size() - 1) {

                    text.append(" | ");
                }
            }

            text.append(" ]\n");
        }

        text.append("\nCapacity : ")
                .append(queue.getCapacity());

        queueArea.setText(
                text.toString()
        );

        frontLabel.setText(
                String.valueOf(
                        queue.getFront()
                )
        );

        rearLabel.setText(
                String.valueOf(
                        queue.getRear()
                )
        );

        sizeLabel.setText(
                queue.size()
                        + " / "
                        + queue.getCapacity()
        );

        queueStatusLabel.setText(
                queue.getStatus()
        );

        if (queue.isFull()) {

            queueStatusLabel.setForeground(
                    Color.RED
            );

        } else if (queue.isEmpty()) {

            queueStatusLabel.setForeground(
                    Color.GRAY
            );

        } else {

            queueStatusLabel.setForeground(
                    new Color(20, 140, 70)
            );
        }
    }

    // =========================================================
    // STACK DISPLAY
    // =========================================================

    private void updateStackDisplay() {

        Memory memory =
                cpu.getDataMemory();

        int sp =
                cpu.getStackPointer().getValue();

        StringBuilder text =
                new StringBuilder();

        text.append(
                String.format(
                        "Stack Pointer (SP): %02X\n\n",
                        sp
                )
        );

        text.append(
                "Address    Value\n"
        );

        text.append(
                "----------------\n"
        );

        int start = sp;

        for (int i = 0; i < 12; i++) {

            int address =
                    start - i;

            if (address < 0) {
                break;
            }

            text.append(
                    String.format(
                            "   %02X       %02X\n",
                            address,
                            memory.read(address)
                    )
            );
        }

        text.append(
                "\nPUSH / POP use Data Memory."
        );

        stackArea.setText(
                text.toString()
        );
    }

    // =========================================================
    // MEMORY DISPLAY
    // =========================================================

    private void updateMemoryDisplay() {

        Memory memory =
                cpu.getDataMemory();

        StringBuilder text =
                new StringBuilder();

        text.append(
                "Addr       Value       Addr       Value\n"
        );

        text.append(
                "----------------------------------------\n"
        );

        for (int i = 0; i < 128; i++) {

            int second =
                    i + 128;

            text.append(
                    String.format(
                            "%02X         %02X",
                            i,
                            memory.read(i)
                    )
            );

            text.append(
                    String.format(
                            "          %02X         %02X",
                            second,
                            memory.read(second)
                    )
            );

            text.append("\n");
        }

        memoryArea.setText(
                text.toString()
        );
    }

    // =========================================================
    // TRACE
    // =========================================================

    private void addTrace(String message) {

        traceArea.append(
                String.format(
                        "[%03d] %s\n",
                        traceNumber++,
                        message
                )
        );

        traceArea.setCaretPosition(
                traceArea.getDocument().getLength()
        );
    }

    // =========================================================
    // CHANGE DETECTION
    // =========================================================

    private void savePreviousState() {

        oldACC =
                cpu.getRegisters().getACC();

        oldB =
                cpu.getRegisters().getB();

        oldR0 =
                cpu.getRegisters().getR(0);

        oldR1 =
                cpu.getRegisters().getR(1);

        oldCarry =
                cpu.getFlags().isCarry();

        oldSP =
                cpu.getStackPointer().getValue();

        oldQueueSize =
                cpu.getQueue().size();
    }

    private void showChanges() {

        int newACC =
                cpu.getRegisters().getACC();

        int newB =
                cpu.getRegisters().getB();

        int newR0 =
                cpu.getRegisters().getR(0);

        int newR1 =
                cpu.getRegisters().getR(1);

        boolean newCarry =
                cpu.getFlags().isCarry();

        int newSP =
                cpu.getStackPointer().getValue();

        int newQueueSize =
                cpu.getQueue().size();

        StringBuilder changes =
                new StringBuilder();

        changes.append(
                "Instruction: "
        );

        if (cpu.getCurrentInstruction() != null) {

            changes.append(
                    cpu.getCurrentInstruction()
                            .toString()
            );

        } else {

            changes.append("NONE");
        }

        changes.append("\n");
        changes.append(
                "----------------------------------\n"
        );

        if (newACC != oldACC) {

            changes.append(
                    String.format(
                            "ACC: %02X -> %02X\n",
                            oldACC,
                            newACC
                    )
            );
        }

        if (newB != oldB) {

            changes.append(
                    String.format(
                            "B:   %02X -> %02X\n",
                            oldB,
                            newB
                    )
            );
        }

        if (newR0 != oldR0) {

            changes.append(
                    String.format(
                            "R0:  %02X -> %02X\n",
                            oldR0,
                            newR0
                    )
            );
        }

        if (newR1 != oldR1) {

            changes.append(
                    String.format(
                            "R1:  %02X -> %02X\n",
                            oldR1,
                            newR1
                    )
            );
        }

        if (newCarry != oldCarry) {

            changes.append(
                    String.format(
                            "CY:  %d -> %d\n",
                            oldCarry ? 1 : 0,
                            newCarry ? 1 : 0
                    )
            );
        }

        if (newSP != oldSP) {

            changes.append(
                    String.format(
                            "SP:  %02X -> %02X\n",
                            oldSP,
                            newSP
                    )
            );
        }

        if (newQueueSize != oldQueueSize) {

            changes.append(
                    String.format(
                            "Queue Size: %d -> %d\n",
                            oldQueueSize,
                            newQueueSize
                    )
            );
        }

        if (changes.toString().endsWith(
                "----------------------------------\n"
        )) {

            changes.append(
                    "No register/queue changes."
            );
        }

        changesArea.append(
                changes.toString()
        );

        changesArea.append(
                "\n\n"
        );

        changesArea.setCaretPosition(
                changesArea.getDocument().getLength()
        );

        savePreviousState();
    }

    // =========================================================
    // MAIN
    // =========================================================

    public static void main(String[] args) {

        SwingUtilities.invokeLater(
                SimulatorUI::new
        );
    }
}