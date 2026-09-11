package src.cpu;

import src.memory.Memory;
import src.memory.ProgramMemory;
import src.instruction.Instruction;
import src.instruction.DecodedInstruction;
import src.instruction.InstructionSet;
import src.queue.FIFOQueue;

public class CPU {

    private Registers registers;
    private StatusFlags flags;
    private StackPointer stackPointer;

    private Memory dataMemory;
    private ProgramMemory programMemory;

    private Instruction currentInstruction;
    private DecodedInstruction decodedInstruction;

    private FIFOQueue queue;

    private boolean halted;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public CPU() {

        registers = new Registers();
        flags = new StatusFlags();
        stackPointer = new StackPointer();

        dataMemory = new Memory();
        programMemory = new ProgramMemory();

        queue = new FIFOQueue();

        halted = false;
    }

    // ============================================================
    // RESET
    // ============================================================

    public void reset() {

        registers.reset();
        flags.reset();
        stackPointer.reset();

        dataMemory.reset();
        programMemory.reset();

        currentInstruction = null;
        decodedInstruction = null;

        queue.reset();

        halted = false;
    }

    // ============================================================
    // LOAD PROGRAM
    // ============================================================

    public void loadProgram(String programText) {

        if (programText == null ||
                programText.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Program is empty."
            );
        }

        programMemory.reset();

        registers.setPC(0);

        currentInstruction = null;
        decodedInstruction = null;

        halted = false;

        String[] lines =
                programText.split("\\r?\\n");

        int address = 0;

        for (String line : lines) {

            line = line.trim();

            // Ignore empty lines
            if (line.isEmpty()) {
                continue;
            }

            // Ignore comments
            if (line.startsWith("//") ||
                    line.startsWith(";")) {
                continue;
            }

            // Remove address prefix
            // Example: 0000: MOV A,#05

            if (line.matches(
                    "^[0-9A-Fa-f]+\\s*:\\s*.*$")) {

                line =
                        line.substring(
                                line.indexOf(":") + 1
                        ).trim();
            }

            if (line.isEmpty()) {
                continue;
            }

            int size =
                    getInstructionSize(line);

            if (address >= programMemory.getSize()) {

                throw new IllegalArgumentException(
                        "Program is too large."
                );
            }

            programMemory.load(
                    address,
                    new Instruction(line, size)
            );

            address += size;
        }
    }

    // ============================================================
    // INSTRUCTION SIZE
    // ============================================================

    public int getInstructionSize(String text) {

        DecodedInstruction decoded =
                InstructionSet.decode(
                        new Instruction(text, 1)
                );

        String mnemonic =
                decoded.getMnemonic();

        String[] operands =
                decoded.getOperands();

        switch (mnemonic) {

            // ----------------------------------------------------
            // MOV
            // ----------------------------------------------------

            case "MOV":

                if (operands.length != 2) {
                    break;
                }

                // MOV B,#data
                if (operands[0].equalsIgnoreCase("B") &&
                        operands[1].startsWith("#")) {

                    return 2;
                }

                // MOV A,#data
                if (operands[0].equalsIgnoreCase("A") &&
                        operands[1].startsWith("#")) {

                    return 2;
                }

                // MOV Rn,#data
                if (isRegister(operands[0]) &&
                        operands[1].startsWith("#")) {

                    return 2;
                }

                // MOV Rn,A
                // MOV A,Rn
                if ((isRegister(operands[0]) &&
                        operands[1].equalsIgnoreCase("A"))
                        ||
                        (operands[0].equalsIgnoreCase("A") &&
                                isRegister(operands[1]))) {

                    return 1;
                }

                break;

            // ----------------------------------------------------
            // ADD
            // ----------------------------------------------------

            case "ADD":

                if (operands.length == 2 &&
                        operands[0].equalsIgnoreCase("A")) {

                    if (operands[1].startsWith("#")) {
                        return 2;
                    }

                    if (isRegister(operands[1])) {
                        return 1;
                    }
                }

                break;

            // ----------------------------------------------------
            // SUBB
            // ----------------------------------------------------

            case "SUBB":

                if (operands.length == 2 &&
                        operands[0].equalsIgnoreCase("A")) {

                    if (operands[1].startsWith("#")) {
                        return 2;
                    }

                    if (isRegister(operands[1])) {
                        return 1;
                    }
                }

                break;

            // ----------------------------------------------------
            // MUL
            // ----------------------------------------------------

            case "MUL":

                if (operands.length == 1 &&
                        operands[0].equalsIgnoreCase("AB")) {

                    return 1;
                }

                break;

            // ----------------------------------------------------
            // DIV
            // ----------------------------------------------------

            case "DIV":

                if (operands.length == 1 &&
                        operands[0].equalsIgnoreCase("AB")) {

                    return 1;
                }

                break;

            // ----------------------------------------------------
            // INC / DEC
            // ----------------------------------------------------

            case "INC":
            case "DEC":

                if (operands.length == 1) {

                    if (operands[0].equalsIgnoreCase("A") ||
                            isRegister(operands[0])) {

                        return 1;
                    }
                }

                break;

            // ----------------------------------------------------
            // ANL / ORL / XRL
            // ----------------------------------------------------

            case "ANL":
            case "ORL":
            case "XRL":

                if (operands.length == 2 &&
                        operands[0].equalsIgnoreCase("A")) {

                    if (operands[1].startsWith("#")) {
                        return 2;
                    }

                    if (isRegister(operands[1])) {
                        return 1;
                    }
                }

                break;

            // ----------------------------------------------------
            // CLR
            // ----------------------------------------------------

            case "CLR":

                if (operands.length == 1 &&
                        (operands[0].equalsIgnoreCase("A") ||
                                operands[0].equalsIgnoreCase("C"))) {

                    return 1;
                }

                break;

            // ----------------------------------------------------
            // SJMP
            // ----------------------------------------------------

            case "SJMP":

                if (operands.length == 1) {
                    return 2;
                }

                break;

            // ----------------------------------------------------
            // ENQ
            // ----------------------------------------------------

            case "ENQ":

                if (operands.length == 1 &&
                        operands[0].startsWith("#")) {

                    return 2;
                }

                break;

            // ----------------------------------------------------
            // DEQ
            // ----------------------------------------------------

            case "DEQ":

                if (operands.length == 0) {
                    return 1;
                }

                break;

            // ----------------------------------------------------
            // HALT
            // ----------------------------------------------------

            case "HALT":

                if (operands.length == 0) {
                    return 1;
                }

                break;
        }

        throw new IllegalArgumentException(
                "Unsupported or invalid instruction: "
                        + text
        );
    }

    // ============================================================
    // FETCH
    // ============================================================

    public void fetch() {

        if (halted) {
            return;
        }

        int pc =
                registers.getPC();

        currentInstruction =
                programMemory.read(pc);

        if (currentInstruction == null) {

            throw new IllegalStateException(
                    String.format(
                            "No instruction at address %04X",
                            pc
                    )
            );
        }

        // Move PC to next instruction
        registers.setPC(
                pc + currentInstruction.getSize()
        );
    }

    // ============================================================
    // DECODE
    // ============================================================

    public void decode() {

        if (currentInstruction == null) {

            throw new IllegalStateException(
                    "No instruction available for decoding."
            );
        }

        decodedInstruction =
                InstructionSet.decode(
                        currentInstruction
                );
    }

    // ============================================================
    // EXECUTE
    // ============================================================

    public void execute() {

        if (decodedInstruction == null) {

            throw new IllegalStateException(
                    "No decoded instruction."
            );
        }

        String mnemonic =
                decodedInstruction.getMnemonic();

        String[] operands =
                decodedInstruction.getOperands();

        switch (mnemonic) {

            case "MOV":
                executeMOV(operands);
                break;

            case "ADD":
                executeADD(operands);
                break;

            case "SUBB":
                executeSUBB(operands);
                break;

            case "MUL":
                executeMUL(operands);
                break;

            case "DIV":
                executeDIV(operands);
                break;

            case "INC":
                executeINC(operands);
                break;

            case "DEC":
                executeDEC(operands);
                break;

            case "ANL":
                executeANL(operands);
                break;

            case "ORL":
                executeORL(operands);
                break;

            case "XRL":
                executeXRL(operands);
                break;

            case "CLR":
                executeCLR(operands);
                break;

            case "SJMP":
                executeSJMP(operands);
                break;

            case "ENQ":
                executeEnqueue();
                break;

            case "DEQ":
                executeDequeue();
                break;

            case "HALT":
                halted = true;
                break;

            default:

                throw new IllegalArgumentException(
                        "Unsupported instruction: "
                                + mnemonic
                );
        }
    }

    // ============================================================
    // MOV
    // ============================================================

    private void executeMOV(String[] operands) {

        if (operands.length != 2) {

            throw new IllegalArgumentException(
                    "MOV requires two operands."
            );
        }

        String destination =
                operands[0].toUpperCase();

        String source =
                operands[1].toUpperCase();

        // MOV A,#data
        if (destination.equals("A") &&
                source.startsWith("#")) {

            registers.setACC(
                    parseNumber(source.substring(1))
            );

            return;
        }

        // MOV B,#data
        if (destination.equals("B") &&
                source.startsWith("#")) {

            registers.setB(
                    parseNumber(source.substring(1))
            );

            return;
        }

        // MOV Rn,#data
        if (isRegister(destination) &&
                source.startsWith("#")) {

            registers.setR(
                    getRegisterNumber(destination),
                    parseNumber(source.substring(1))
            );

            return;
        }

        // MOV Rn,A
        if (isRegister(destination) &&
                source.equals("A")) {

            registers.setR(
                    getRegisterNumber(destination),
                    registers.getACC()
            );

            return;
        }

        // MOV A,Rn
        if (destination.equals("A") &&
                isRegister(source)) {

            registers.setACC(
                    registers.getR(
                            getRegisterNumber(source)
                    )
            );

            return;
        }

        throw new IllegalArgumentException(
                "Invalid MOV instruction."
        );
    }

    // ============================================================
    // ADD
    // ============================================================

    private void executeADD(String[] operands) {

        if (operands.length != 2 ||
                !operands[0].equalsIgnoreCase("A")) {

            throw new IllegalArgumentException(
                    "ADD syntax: ADD A,#data or ADD A,Rn"
            );
        }

        int value;

        if (operands[1].startsWith("#")) {

            value =
                    parseNumber(
                            operands[1].substring(1)
                    );

        } else if (isRegister(operands[1])) {

            value =
                    registers.getR(
                            getRegisterNumber(
                                    operands[1]
                            )
                    );

        } else {

            throw new IllegalArgumentException(
                    "Invalid ADD operand."
            );
        }

        int accumulator =
                registers.getACC();

        int result =
                accumulator + value;

        flags.setCarry(
                result > 0xFF
        );

        registers.setACC(result);
    }

    // ============================================================
    // SUBB
    // ============================================================

    private void executeSUBB(String[] operands) {

        if (operands.length != 2 ||
                !operands[0].equalsIgnoreCase("A")) {

            throw new IllegalArgumentException(
                    "SUBB syntax: SUBB A,#data or SUBB A,Rn"
            );
        }

        int value;

        if (operands[1].startsWith("#")) {

            value =
                    parseNumber(
                            operands[1].substring(1)
                    );

        } else if (isRegister(operands[1])) {

            value =
                    registers.getR(
                            getRegisterNumber(
                                    operands[1]
                            )
                    );

        } else {

            throw new IllegalArgumentException(
                    "Invalid SUBB operand."
            );
        }

        int carry =
                flags.isCarry() ? 1 : 0;

        int accumulator =
                registers.getACC();

        int result =
                accumulator - value - carry;

        flags.setCarry(
                result < 0
        );

        registers.setACC(result);
    }

    // ============================================================
    // MUL AB
    // ============================================================

    private void executeMUL(String[] operands) {

        if (operands.length != 1 ||
                !operands[0].equalsIgnoreCase("AB")) {

            throw new IllegalArgumentException(
                    "MUL syntax: MUL AB"
            );
        }

        int A =
                registers.getACC();

        int B =
                registers.getB();

        int result =
                A * B;

        registers.setACC(
                result & 0xFF
        );

        registers.setB(
                (result >> 8) & 0xFF
        );

        flags.setCarry(false);

        flags.setOverflow(
                result > 0xFF
        );
    }

    // ============================================================
    // DIV AB
    // ============================================================

    private void executeDIV(String[] operands) {

        if (operands.length != 1 ||
                !operands[0].equalsIgnoreCase("AB")) {

            throw new IllegalArgumentException(
                    "DIV syntax: DIV AB"
            );
        }

        int A =
                registers.getACC();

        int B =
                registers.getB();

        if (B == 0) {

            throw new ArithmeticException(
                    "Division by zero is not allowed."
            );
        }

        int quotient =
                A / B;

        int remainder =
                A % B;

        registers.setACC(quotient);
        registers.setB(remainder);

        flags.setCarry(false);
        flags.setOverflow(false);
    }

    // ============================================================
    // INC
    // ============================================================

    private void executeINC(String[] operands) {

        if (operands.length != 1) {

            throw new IllegalArgumentException(
                    "INC requires one operand."
            );
        }

        String operand =
                operands[0].toUpperCase();

        if (operand.equals("A")) {

            registers.setACC(
                    registers.getACC() + 1
            );

        } else if (isRegister(operand)) {

            int registerNumber =
                    getRegisterNumber(operand);

            registers.setR(
                    registerNumber,
                    registers.getR(registerNumber) + 1
            );

        } else {

            throw new IllegalArgumentException(
                    "Invalid INC operand."
            );
        }
    }

    // ============================================================
    // DEC
    // ============================================================

    private void executeDEC(String[] operands) {

        if (operands.length != 1) {

            throw new IllegalArgumentException(
                    "DEC requires one operand."
            );
        }

        String operand =
                operands[0].toUpperCase();

        if (operand.equals("A")) {

            registers.setACC(
                    registers.getACC() - 1
            );

        } else if (isRegister(operand)) {

            int registerNumber =
                    getRegisterNumber(operand);

            registers.setR(
                    registerNumber,
                    registers.getR(registerNumber) - 1
            );

        } else {

            throw new IllegalArgumentException(
                    "Invalid DEC operand."
            );
        }
    }

    // ============================================================
    // ANL
    // ============================================================

    private void executeANL(String[] operands) {

        if (operands.length != 2 ||
                !operands[0].equalsIgnoreCase("A")) {

            throw new IllegalArgumentException(
                    "ANL syntax: ANL A,#data or ANL A,Rn"
            );
        }

        int value;

        if (operands[1].startsWith("#")) {

            value =
                    parseNumber(
                            operands[1].substring(1)
                    );

        } else if (isRegister(operands[1])) {

            value =
                    registers.getR(
                            getRegisterNumber(
                                    operands[1]
                            )
                    );

        } else {

            throw new IllegalArgumentException(
                    "Invalid ANL operand."
            );
        }

        registers.setACC(
                registers.getACC() & value
        );
    }

    // ============================================================
    // ORL
    // ============================================================

    private void executeORL(String[] operands) {

        if (operands.length != 2 ||
                !operands[0].equalsIgnoreCase("A")) {

            throw new IllegalArgumentException(
                    "ORL syntax: ORL A,#data or ORL A,Rn"
            );
        }

        int value;

        if (operands[1].startsWith("#")) {

            value =
                    parseNumber(
                            operands[1].substring(1)
                    );

        } else if (isRegister(operands[1])) {

            value =
                    registers.getR(
                            getRegisterNumber(
                                    operands[1]
                            )
                    );

        } else {

            throw new IllegalArgumentException(
                    "Invalid ORL operand."
            );
        }

        registers.setACC(
                registers.getACC() | value
        );
    }

    // ============================================================
    // XRL
    // ============================================================

    private void executeXRL(String[] operands) {

        if (operands.length != 2 ||
                !operands[0].equalsIgnoreCase("A")) {

            throw new IllegalArgumentException(
                    "XRL syntax: XRL A,#data or XRL A,Rn"
            );
        }

        int value;

        if (operands[1].startsWith("#")) {

            value =
                    parseNumber(
                            operands[1].substring(1)
                    );

        } else if (isRegister(operands[1])) {

            value =
                    registers.getR(
                            getRegisterNumber(
                                    operands[1]
                            )
                    );

        } else {

            throw new IllegalArgumentException(
                    "Invalid XRL operand."
            );
        }

        registers.setACC(
                registers.getACC() ^ value
        );
    }

    // ============================================================
    // CLR
    // ============================================================

    private void executeCLR(String[] operands) {

        if (operands.length != 1) {

            throw new IllegalArgumentException(
                    "CLR requires one operand."
            );
        }

        String operand =
                operands[0].toUpperCase();

        if (operand.equals("A")) {

            registers.setACC(0);

        } else if (operand.equals("C")) {

            flags.setCarry(false);

        } else {

            throw new IllegalArgumentException(
                    "CLR supports A or C."
            );
        }
    }

    // ============================================================
    // SJMP
    // ============================================================

    private void executeSJMP(String[] operands) {

        if (operands.length != 1) {

            throw new IllegalArgumentException(
                    "SJMP requires one operand."
            );
        }

        int target =
                parseNumber(operands[0]);

        registers.setPC(target);
    }

    // ============================================================
    // ENQUEUE
    // ============================================================

    private void executeEnqueue() {

        if (decodedInstruction.getOperands().length != 1) {

            throw new IllegalArgumentException(
                    "ENQ syntax: ENQ #data"
            );
        }

        String operand =
                decodedInstruction.getOperands()[0];

        int value =
                parseImmediate(operand);

        queue.enqueue(value);
    }

    // ============================================================
    // DEQUEUE
    // ============================================================

    private void executeDequeue() {

        if (queue.isEmpty()) {

            throw new IllegalStateException(
                    "Cannot DEQ: Queue is EMPTY"
            );
        }

        int value =
                queue.dequeue();

        // Put dequeued value into accumulator
        registers.setACC(value);
    }

    // ============================================================
    // STEP
    // ============================================================

    public void step() {

        if (halted) {
            return;
        }

        fetch();
        decode();
        execute();
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    private boolean isRegister(String text) {

        if (text == null ||
                text.length() != 2) {

            return false;
        }

        char first =
                Character.toUpperCase(
                        text.charAt(0)
                );

        char second =
                text.charAt(1);

        return first == 'R' &&
                second >= '0' &&
                second <= '7';
    }

    private int getRegisterNumber(String text) {

        if (!isRegister(text)) {

            throw new IllegalArgumentException(
                    "Invalid register: " + text
            );
        }

        return text.charAt(1) - '0';
    }

    private int parseNumber(String text) {

        text =
                text.trim()
                        .toUpperCase();

        if (text.startsWith("0X")) {

            return Integer.parseInt(
                    text.substring(2),
                    16
            );
        }

        // Hex values containing A-F
        if (text.matches(".*[A-F].*")) {

            return Integer.parseInt(
                    text,
                    16
            );
        }

        return Integer.parseInt(text);
    }

    // ============================================================
    // PARSE IMMEDIATE
    // ============================================================

    private int parseImmediate(String operand) {

        if (operand == null) {

            throw new IllegalArgumentException(
                    "Immediate operand is missing."
            );
        }

        operand =
                operand.trim();

        // Remove #
        if (operand.startsWith("#")) {

            operand =
                    operand.substring(1);
        }

        operand =
                operand.trim();

        // 0x format
        if (operand.startsWith("0x") ||
                operand.startsWith("0X")) {

            return Integer.parseInt(
                    operand.substring(2),
                    16
            );
        }

        // H format
        if (operand.endsWith("H") ||
                operand.endsWith("h")) {

            return Integer.parseInt(
                    operand.substring(
                            0,
                            operand.length() - 1
                    ),
                    16
            );
        }

        // Decimal
        return Integer.parseInt(operand);
    }

    // ============================================================
    // GETTERS
    // ============================================================

    public Registers getRegisters() {
        return registers;
    }

    public StatusFlags getFlags() {
        return flags;
    }

    public StackPointer getStackPointer() {
        return stackPointer;
    }

    public Memory getDataMemory() {
        return dataMemory;
    }

    public ProgramMemory getProgramMemory() {
        return programMemory;
    }

    public Instruction getCurrentInstruction() {
        return currentInstruction;
    }

    public DecodedInstruction getDecodedInstruction() {
        return decodedInstruction;
    }

    public FIFOQueue getQueue() {
        return queue;
    }

    public boolean isHalted() {
        return halted;
    }

    public void halt() {
        halted = true;
    }

    // ============================================================
    // CPU STATE
    // ============================================================

    public void printCPUState() {

        System.out.println(
                registers.toString()
        );

        System.out.println(
                "Flags = " + flags.toString()
        );

        System.out.println(
                "SP = "
                        + String.format(
                                "%02X",
                                stackPointer.getValue()
                        )
        );

        System.out.println(
                "Queue = " + queue
        );

        System.out.println(
                "Queue Status = "
                        + queue.getStatus()
        );

        System.out.println(
                "Halted = " + halted
        );
    }
}