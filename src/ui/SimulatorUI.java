package src.ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

import src.cpu.CPU;

public class SimulatorUI extends JFrame {

    private CPU cpu;

    private JButton loadButton;
    private JButton resetButton;
    private JButton stepButton;
    private JButton runButton;

    private JTextArea programArea;
    private JTextArea traceArea;
    private JTextArea changesArea;

    private JLabel currentInstructionLabel;
    private JLabel pcLabel;
    private JLabel accumulatorLabel;
    private JLabel r0Label;
    private JLabel r1Label;

    private JLabel carryFlagLabel;
    private JLabel zeroFlagLabel;
    private JLabel statusLabel;

    public SimulatorUI() {

        cpu = new CPU();

        setTitle("Nuvoton MS51FB9AE Microcontroller Simulator");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initializeComponents();

        setVisible(true);
    }

    private void initializeComponents() {

        setLayout(new BorderLayout(10, 10));

        // ==========================================
        // TOP PANEL - BUTTONS
        // ==========================================

        JPanel buttonPanel = new JPanel();

        loadButton = new JButton("LOAD");
        resetButton = new JButton("RESET");
        stepButton = new JButton("STEP");
        runButton = new JButton("RUN");

        buttonPanel.add(loadButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(stepButton);
        buttonPanel.add(runButton);

        add(buttonPanel, BorderLayout.NORTH);

        // ==========================================
        // LEFT - PROGRAM
        // ==========================================

        programArea = new JTextArea();
        programArea.setEditable(false);
        programArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        programArea.setBorder(
            BorderFactory.createTitledBorder("Program Memory")
        );

        JScrollPane programScroll =
            new JScrollPane(programArea);

        add(programScroll, BorderLayout.WEST);

        programScroll.setPreferredSize(
            new Dimension(300, 500)
        );

        // ==========================================
        // CENTER - TRACE
        // ==========================================

        traceArea = new JTextArea();
        traceArea.setEditable(false);
        traceArea.setFont(
            new Font("Monospaced", Font.PLAIN, 14)
        );

        traceArea.setText(
            "Simulator ready.\n"
        );

        traceArea.setBorder(
            BorderFactory.createTitledBorder(
                "FETCH → DECODE → EXECUTE Trace"
            )
        );

        JScrollPane traceScroll =
            new JScrollPane(traceArea);

        add(traceScroll, BorderLayout.CENTER);

        // ==========================================
        // RIGHT - CPU STATE
        // ==========================================

        JPanel cpuPanel =
            new JPanel(new GridLayout(0, 1, 5, 5));

        cpuPanel.setBorder(
            BorderFactory.createTitledBorder(
                "CPU State"
            )
        );

        currentInstructionLabel =
            new JLabel("Instruction: None");

        pcLabel =
            new JLabel("PC = 0000");

        accumulatorLabel =
            new JLabel("A = 00");

        r0Label =
            new JLabel("R0 = 00");

        r1Label =
            new JLabel("R1 = 00");

        carryFlagLabel =
            new JLabel("Carry = 0");

        zeroFlagLabel =
            new JLabel("Zero = 0");

        statusLabel =
            new JLabel("Status: READY");

        cpuPanel.add(currentInstructionLabel);
        cpuPanel.add(pcLabel);
        cpuPanel.add(accumulatorLabel);
        cpuPanel.add(r0Label);
        cpuPanel.add(r1Label);
        cpuPanel.add(carryFlagLabel);
        cpuPanel.add(zeroFlagLabel);
        cpuPanel.add(statusLabel);

        add(cpuPanel, BorderLayout.EAST);

        cpuPanel.setPreferredSize(
            new Dimension(230, 500)
        );

        // ==========================================
        // BOTTOM - CHANGES
        // ==========================================

        changesArea = new JTextArea();
        changesArea.setEditable(false);
        changesArea.setFont(
            new Font("Monospaced", Font.PLAIN, 13)
        );

        changesArea.setBorder(
            BorderFactory.createTitledBorder(
                "Register / Memory Changes"
            )
        );

        JScrollPane changesScroll =
            new JScrollPane(changesArea);

        changesScroll.setPreferredSize(
            new Dimension(1000, 150)
        );

        add(changesScroll, BorderLayout.SOUTH);

        // ==========================================
        // BUTTON ACTIONS
        // ==========================================

        loadButton.addActionListener(e -> loadProgram());

        resetButton.addActionListener(e -> resetCPU());

        stepButton.addActionListener(e -> stepCPU());

        runButton.addActionListener(e -> runCPU());
    }

    // ==========================================
    // LOAD
    // ==========================================

    private void loadProgram() {

        cpu.reset();

        cpu.loadProgram();

        programArea.setText("");

        programArea.append(
            "0000: MOV A, #05\n"
        );

        programArea.append(
            "0002: MOV R1, #03\n"
        );

        programArea.append(
            "0004: ADD A, R1\n"
        );

        programArea.append(
            "0005: INC A\n"
        );

        programArea.append(
            "0006: ANL A, #0F\n"
        );

        programArea.append(
            "0008: DEC A\n"
        );

        programArea.append(
            "0009: SJMP 000B\n"
        );

        programArea.append(
            "000B: HALT\n"
        );

        traceArea.setText(
            "PROGRAM LOADED\n"
            + "PC = 0000\n\n"
        );

        changesArea.setText(
            "Program loaded successfully.\n"
        );

        statusLabel.setText(
            "Status: LOADED"
        );

        updateCPUDisplay();
    }

    // ==========================================
    // RESET
    // ==========================================

    private void resetCPU() {

        cpu.reset();

        programArea.setText("");

        traceArea.setText(
            "CPU RESET\n"
        );

        changesArea.setText(
            "CPU registers and memory reset.\n"
        );

        currentInstructionLabel.setText(
            "Instruction: None"
        );

        statusLabel.setText(
            "Status: READY"
        );

        updateCPUDisplay();
    }

    // ==========================================
    // STEP
    // ==========================================

    private void stepCPU() {

    // Check whether a program has been loaded
    if (cpu.getProgramMemory().read(0) == null
            && cpu.getRegisters().getPC() == 0) {

        JOptionPane.showMessageDialog(
            this,
            "No program loaded.\n\nPlease click LOAD before using STEP or RUN.",
            "Program Not Loaded",
            JOptionPane.WARNING_MESSAGE
        );

        return;
    }

    try {

        if (cpu.isHalted()) {

            JOptionPane.showMessageDialog(
                this,
                "The CPU has already reached HALT.",
                "CPU Halted",
                JOptionPane.INFORMATION_MESSAGE
            );

            return;
        }

        int oldPC = cpu.getRegisters().getPC();
        int oldA = cpu.getRegisters().getACC();
        int oldR0 = cpu.getRegisters().getR(0);
        int oldR1 = cpu.getRegisters().getR(1);

        // FETCH
        cpu.fetch();

        traceArea.append(
            "FETCH   → "
            + cpu.getCurrentInstruction()
            + "\n"
        );

        // DECODE
        cpu.decode();

        traceArea.append(
            "DECODE  → "
            + cpu.getDecodedInstruction()
            + "\n"
        );

        // EXECUTE
        cpu.execute();

        traceArea.append(
            "EXECUTE → completed\n"
        );

        traceArea.append(
            "-------------------------\n"
        );

        // Show changes
        changesArea.setText("");

        if (oldA != cpu.getRegisters().getACC()) {
            changesArea.append(
                "ACC: "
                + String.format("%02X", oldA)
                + " → "
                + String.format(
                    "%02X",
                    cpu.getRegisters().getACC()
                )
                + "\n"
            );
        }

        if (oldR0 != cpu.getRegisters().getR(0)) {
            changesArea.append(
                "R0: "
                + String.format("%02X", oldR0)
                + " → "
                + String.format(
                    "%02X",
                    cpu.getRegisters().getR(0)
                )
                + "\n"
            );
        }

        if (oldR1 != cpu.getRegisters().getR(1)) {
            changesArea.append(
                "R1: "
                + String.format("%02X", oldR1)
                + " → "
                + String.format(
                    "%02X",
                    cpu.getRegisters().getR(1)
                )
                + "\n"
            );
        }

        if (oldPC != cpu.getRegisters().getPC()) {
            changesArea.append(
                "PC: "
                + String.format("%04X", oldPC)
                + " → "
                + String.format(
                    "%04X",
                    cpu.getRegisters().getPC()
                )
                + "\n"
            );
        }

        if (cpu.isHalted()) {

            changesArea.append(
                "CPU reached HALT.\n"
            );

            statusLabel.setText(
                "Status: HALTED"
            );

        } else {

            statusLabel.setText(
                "Status: RUNNING"
            );
        }

        updateCPUDisplay();

    } catch (Exception ex) {

        JOptionPane.showMessageDialog(
            this,
            "Execution Error:\n\n" + ex.getMessage(),
            "Execution Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
}
    // ==========================================
    // RUN
    // ==========================================
private void runCPU() {

    // Check whether a program has been loaded
    if (cpu.getProgramMemory().read(0) == null
            && cpu.getRegisters().getPC() == 0) {

        JOptionPane.showMessageDialog(
            this,
            "No program loaded.\n\nPlease click LOAD before using RUN.",
            "Program Not Loaded",
            JOptionPane.WARNING_MESSAGE
        );

        return;
    }

    try {

        int safetyCounter = 0;

        while (!cpu.isHalted()
                && safetyCounter < 100) {

            stepCPU();

            safetyCounter++;
        }

        if (cpu.isHalted()) {

            statusLabel.setText(
                "Status: HALTED"
            );

        } else if (safetyCounter >= 100) {

            statusLabel.setText(
                "Status: STOPPED - LOOP LIMIT"
            );
        }

    } catch (Exception ex) {

        JOptionPane.showMessageDialog(
            this,
            "Execution Error:\n\n" + ex.getMessage(),
            "Execution Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
}
    
    // ==========================================
    // UPDATE CPU DISPLAY
    // ==========================================

    private void updateCPUDisplay() {

        pcLabel.setText(
            "PC = "
            + String.format(
                "%04X",
                cpu.getRegisters().getPC()
            )
        );

        accumulatorLabel.setText(
            "A = "
            + String.format(
                "%02X",
                cpu.getRegisters().getACC()
            )
        );

        r0Label.setText(
            "R0 = "
            + String.format(
                "%02X",
                cpu.getRegisters().getR(0)
            )
        );

        r1Label.setText(
            "R1 = "
            + String.format(
                "%02X",
                cpu.getRegisters().getR(1)
            )
        );

        carryFlagLabel.setText(
            "Carry = "
            + (cpu.getFlags().isCarry() ? "1" : "0")
        );

        if (cpu.getCurrentInstruction() != null) {

            currentInstructionLabel.setText(
                "Instruction: "
                + cpu.getCurrentInstruction()
            );
        }
    }
}