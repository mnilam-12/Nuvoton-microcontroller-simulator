import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class SimulatorUI extends JFrame {

    // =========================
    // BUTTONS
    // =========================

    private JButton loadButton;
    private JButton resetButton;
    private JButton stepButton;
    private JButton runButton;


    // =========================
    // DISPLAY COMPONENTS
    // =========================

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


    // =========================
    // CONSTRUCTOR
    // =========================

    public SimulatorUI() {

        setTitle("Nuvoton MS51FB9AE Microcontroller Simulator");

        setSize(1100, 750);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLocationRelativeTo(null);

        initializeComponents();

        setVisible(true);
    }


    // =========================
    // INITIALIZE UI
    // =========================

    private void initializeComponents() {

        // Main Panel
        JPanel mainPanel =
                new JPanel(new BorderLayout(10, 10));

        mainPanel.setBorder(
                new EmptyBorder(15, 15, 15, 15)
        );


        // =========================
        // HEADER
        // =========================

        JLabel titleLabel = new JLabel(
                "NUVOTON MS51FB9AE MICROCONTROLLER SIMULATOR",
                SwingConstants.CENTER
        );

        titleLabel.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        22
                )
        );

        mainPanel.add(
                titleLabel,
                BorderLayout.NORTH
        );


        // =========================
        // CENTER AREA
        // =========================

        JPanel centerPanel =
                new JPanel(
                        new GridLayout(
                                1,
                                2,
                                15,
                                15
                        )
                );


        // =========================
        // LEFT SIDE - PROGRAM MEMORY
        // =========================

        JPanel leftPanel =
                new JPanel(
                        new BorderLayout(
                                10,
                                10
                        )
                );

        leftPanel.setBorder(
                new TitledBorder(
                        "Program Memory"
                )
        );


        programArea =
                new JTextArea();

        programArea.setFont(
                new Font(
                        "Monospaced",
                        Font.PLAIN,
                        15
                )
        );

        programArea.setEditable(false);


        // Demo Program

        programArea.setText(

                "0000: MOV A, #05\n" +
                "0001: MOV R1, #03\n" +
                "0002: ADD A, R1\n" +
                "0003: INC A\n" +
                "0004: ANL A, #0F\n" +
                "0005: DEC A\n" +
                "0006: SJMP 0007\n" +
                "0007: HALT"

        );


        leftPanel.add(
                new JScrollPane(
                        programArea
                ),
                BorderLayout.CENTER
        );


        // =========================
        // RIGHT SIDE - CPU STATE
        // =========================

        JPanel rightPanel =
                new JPanel();

        rightPanel.setLayout(
                new BoxLayout(
                        rightPanel,
                        BoxLayout.Y_AXIS
                )
        );


        // =========================
        // CURRENT INSTRUCTION
        // =========================

        JPanel instructionPanel =
                createInfoPanel(
                        "Current Instruction"
                );

        currentInstructionLabel =
                new JLabel(
                        "No instruction loaded"
                );

        instructionPanel.add(
                currentInstructionLabel
        );


        // =========================
        // PROGRAM COUNTER
        // =========================

        JPanel pcPanel =
                createInfoPanel(
                        "Program Counter"
                );

        pcLabel =
                new JLabel(
                        "0000"
                );

        pcPanel.add(
                pcLabel
        );


        // =========================
        // REGISTERS
        // =========================

        JPanel registerPanel =
                createInfoPanel(
                        "Registers"
                );

        accumulatorLabel =
                new JLabel(
                        "A = 00"
                );

        r0Label =
                new JLabel(
                        "R0 = 00"
                );

        r1Label =
                new JLabel(
                        "R1 = 00"
                );


        registerPanel.add(
                accumulatorLabel
        );

        registerPanel.add(
                r0Label
        );

        registerPanel.add(
                r1Label
        );


        // =========================
        // FLAGS
        // =========================

        JPanel flagPanel =
                createInfoPanel(
                        "Flags"
                );

        carryFlagLabel =
                new JLabel(
                        "Carry = 0"
                );

        zeroFlagLabel =
                new JLabel(
                        "Zero = 0"
                );


        flagPanel.add(
                carryFlagLabel
        );

        flagPanel.add(
                zeroFlagLabel
        );


        // =========================
        // EXECUTION STATUS
        // =========================

        JPanel statusPanel =
                createInfoPanel(
                        "Execution Status"
                );

        statusLabel =
                new JLabel(
                        "READY"
                );

        statusPanel.add(
                statusLabel
        );


        // Add CPU panels

        rightPanel.add(
                instructionPanel
        );

        rightPanel.add(
                pcPanel
        );

        rightPanel.add(
                registerPanel
        );

        rightPanel.add(
                flagPanel
        );

        rightPanel.add(
                statusPanel
        );


        // Add left and right panels

        centerPanel.add(
                leftPanel
        );

        centerPanel.add(
                rightPanel
        );


        mainPanel.add(
                centerPanel,
                BorderLayout.CENTER
        );


        // =========================
        // EXECUTION TRACE
        // =========================

        JPanel bottomPanel =
                new JPanel(
                        new GridLayout(
                                1,
                                2,
                                15,
                                15
                        )
                );


        JPanel tracePanel =
                new JPanel(
                        new BorderLayout()
                );

        tracePanel.setBorder(
                new TitledBorder(
                        "Execution Trace"
                )
        );


        traceArea =
                new JTextArea();

        traceArea.setFont(
                new Font(
                        "Monospaced",
                        Font.PLAIN,
                        14
                )
        );

        traceArea.setEditable(false);


        traceArea.setText(

                "FETCH\n" +
                "DECODE\n" +
                "EXECUTE"

        );


        tracePanel.add(
                new JScrollPane(
                        traceArea
                ),
                BorderLayout.CENTER
        );


        // =========================
        // STATE CHANGES
        // =========================

        JPanel changesPanel =
                new JPanel(
                        new BorderLayout()
                );

        changesPanel.setBorder(
                new TitledBorder(
                        "State Changes"
                )
        );


        changesArea =
                new JTextArea();

        changesArea.setFont(
                new Font(
                        "Monospaced",
                        Font.PLAIN,
                        14
                )
        );

        changesArea.setEditable(false);


        changesArea.setText(
                "No changes yet"
        );


        changesPanel.add(
                new JScrollPane(
                        changesArea
                ),
                BorderLayout.CENTER
        );


        // Add both panels

        bottomPanel.add(
                tracePanel
        );

        bottomPanel.add(
                changesPanel
        );


        // =========================
        // BUTTONS
        // =========================

        JPanel buttonPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.CENTER,
                                20,
                                10
                        )
                );


        loadButton =
                new JButton(
                        "LOAD"
                );

        resetButton =
                new JButton(
                        "RESET"
                );

        stepButton =
                new JButton(
                        "STEP"
                );

        runButton =
                new JButton(
                        "RUN"
                );


        buttonPanel.add(
                loadButton
        );

        buttonPanel.add(
                resetButton
        );

        buttonPanel.add(
                stepButton
        );

        buttonPanel.add(
                runButton
        );


        // =========================
        // COMBINED BOTTOM SECTION
        // =========================

        JPanel southPanel =
                new JPanel(
                        new BorderLayout(
                                10,
                                10
                        )
                );


        southPanel.add(
                bottomPanel,
                BorderLayout.CENTER
        );


        southPanel.add(
                buttonPanel,
                BorderLayout.SOUTH
        );


        mainPanel.add(
                southPanel,
                BorderLayout.SOUTH
        );


        // =========================
        // ADD MAIN PANEL
        // =========================

        add(mainPanel);
    }


    // =========================
    // HELPER METHOD
    // =========================

    private JPanel createInfoPanel(
            String title
    ) {

        JPanel panel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT
                        )
                );

        panel.setBorder(
                new TitledBorder(
                        title
                )
        );

        return panel;
    }


    // =========================
    // MAIN METHOD
    // =========================

    public static void main(
            String[] args
    ) {

        SwingUtilities.invokeLater(
                SimulatorUI::new
        );
    }
}