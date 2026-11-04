
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

        System.out.println("[CPU LOAD] Starting program load");

        if (programText == null || programText.trim().isEmpty()) {
            throw new IllegalArgumentException("Program is empty.");
        }

        programMemory.reset();
        registers.setPC(0);

        currentInstruction = null;
        decodedInstruction = null;
        halted = false;

        String[] lines = programText.split("\\r?\\n");

        int address = 0;
        int loadedInstructions = 0;

        for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {

            String line = lines[lineIndex].trim();

            if (line.isEmpty() ||
                    line.startsWith("//") ||
                    line.startsWith(";")) {
                continue;
            }

            if (line.matches("^[0-9A-Fa-f]+\\s*:\\s*.*$")) {
                line = line.substring(line.indexOf(':') + 1).trim();
            }

            if (line.isEmpty()) {
                continue;
            }

            System.out.println("[CPU LOAD] Decoding source line "
                    + (lineIndex + 1) + ": " + line);

            final int instructionSize;

            try {
                instructionSize = getInstructionSize(line);
            } catch (RuntimeException ex) {
                throw new IllegalArgumentException(
                        "Invalid instruction on line "
                                + (lineIndex + 1)
                                + ": " + line
                                + " (" + ex.getMessage() + ")", ex);
            }

            if (instructionSize <= 0) {
                throw new IllegalArgumentException(
                        "Invalid instruction size on line "
                                + (lineIndex + 1));
            }

            if (address + instructionSize > programMemory.getSize()) {
                throw new IllegalArgumentException(
                        "Program is too large near source line "
                                + (lineIndex + 1)
                                + ". Program memory size is "
                                + programMemory.getSize() + " bytes.");
            }

            System.out.println("[CPU LOAD] Storing instruction at address 0x"
                    + String.format("%04X", address)
                    + " (size " + instructionSize + ")");

            programMemory.load(
                    address,
                    new Instruction(line, instructionSize)
            );

            address += instructionSize;
            loadedInstructions++;
        }

        if (loadedInstructions == 0) {
            throw new IllegalArgumentException(
                    "No valid instructions were found in the program.");
        }

        System.out.println("[CPU LOAD] Completed. Instructions loaded: "
                + loadedInstructions + ", bytes used: " + address);
    }

    // ============================================================
    // INSTRUCTION SIZE
    // ============================================================

    public int getInstructionSize(String text) {

        DecodedInstruction decoded =
                InstructionSet.decode(new Instruction(text, 1));

        String mnemonic = decoded.getMnemonic();
        String[] operands = decoded.getOperands();

        switch (mnemonic) {

            case "MOV":

                if (operands.length != 2) {
                    break;
                }

                if (operands[0].equalsIgnoreCase("B") &&
                        operands[1].startsWith("#")) {
                    return 2;
                }

                if (operands[0].equalsIgnoreCase("A") &&
                        operands[1].startsWith("#")) {
                    return 2;
                }

                if (isRegister(operands[0]) &&
                        operands[1].startsWith("#")) {
                    return 2;
                }

                if ((isRegister(operands[0]) &&
                        operands[1].equalsIgnoreCase("A"))
                        ||
                        (operands[0].equalsIgnoreCase("A") &&
                                isRegister(operands[1]))) {
                    return 1;
                }

                break;

            case "ADD":
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

            case "MUL":
            case "DIV":

                if (operands.length == 1 &&
                        operands[0].equalsIgnoreCase("AB")) {
                    return 1;
                }

                break;

            case "INC":
            case "DEC":

                if (operands.length == 1) {

                    if (operands[0].equalsIgnoreCase("A") ||
                            isRegister(operands[0])) {
                        return 1;
                    }
                }

                break;

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

            case "CLR":

                if (operands.length == 1 &&
                        (operands[0].equalsIgnoreCase("A") ||
                                operands[0].equalsIgnoreCase("C"))) {
                    return 1;
                }

                break;

            case "SJMP":
            case "JZ":
            case "JNZ":
            case "JC":
            case "JNC":

                if (operands.length == 1) {
                    return 2;
                }

                break;

            // LOAD A,address or LOAD A,[Rn]
            case "LOAD":

                if (operands.length == 2 &&
                        operands[0].equalsIgnoreCase("A")) {

                    if (isMemoryRegister(operands[1])) {
                        return 2;
                    }

                    parseNumber(operands[1]);
                    return 2;
                }

                break;

            // STORE address or STORE [Rn],A
            case "STORE":

                if (operands.length == 1) {

                    parseNumber(operands[0]);
                    return 2;
                }

                if (operands.length == 2 &&
                        isMemoryRegister(operands[0]) &&
                        operands[1].equalsIgnoreCase("A")) {
                    return 2;
                }

                break;

            case "SHL":
            case "SHR":

                if (operands.length == 1 &&
                        operands[0].equalsIgnoreCase("A")) {
                    return 1;
                }

                break;

            case "PUSH":
            case "POP":

                if (operands.length == 1) {
                    return 2;
                }

                break;

            case "ENQ":

                if (operands.length == 1) {

                    if (operands[0].equalsIgnoreCase("A")) {
                        return 1;
                    }

                    if (operands[0].startsWith("#")) {
                        return 2;
                    }
                }

                break;

            case "DEQ":

                if (operands.length == 0) {
                    return 1;
                }

                break;

            case "HALT":

                if (operands.length == 0) {
                    return 1;
                }

                break;
        }

        throw new IllegalArgumentException(
                "Unsupported or invalid instruction: " + text
        );
    }

    // ============================================================
    // FETCH
    // ============================================================

    public void fetch() {

        if (halted) {
            return;
        }

        int pc = registers.getPC();

        currentInstruction = programMemory.read(pc);

        if (currentInstruction == null) {
            throw new IllegalStateException(
                    String.format("No instruction at address %04X", pc)
            );
        }

        registers.setPC(pc + currentInstruction.getSize());
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

        decodedInstruction = InstructionSet.decode(currentInstruction);
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

        String mnemonic = decodedInstruction.getMnemonic();
        String[] operands = decodedInstruction.getOperands();

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

            case "JZ":
                executeJZ(operands);
                break;

            case "JNZ":
                executeJNZ(operands);
                break;

            case "JC":
                executeJC(operands);
                break;

            case "JNC":
                executeJNC(operands);
                break;

            case "LOAD":
                executeLOAD(operands);
                break;

            case "STORE":
                executeSTORE(operands);
                break;

            case "SHL":
                executeSHL(operands);
                break;

            case "SHR":
                executeSHR(operands);
                break;

            case "PUSH":
                executePUSH(operands);
                break;

            case "POP":
                executePOP(operands);
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
                        "Unsupported instruction: " + mnemonic
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

        String destination = operands[0].toUpperCase();
        String source = operands[1].toUpperCase();

        if (destination.equals("A") && source.startsWith("#")) {
            registers.setACC(parseNumber(source.substring(1)));
            return;
        }

        if (destination.equals("B") && source.startsWith("#")) {
            registers.setB(parseNumber(source.substring(1)));
            return;
        }

        if (isRegister(destination) && source.startsWith("#")) {
            registers.setR(
                    getRegisterNumber(destination),
                    parseNumber(source.substring(1))
            );
            return;
        }

        if (isRegister(destination) && source.equals("A")) {
            registers.setR(
                    getRegisterNumber(destination),
                    registers.getACC()
            );
            return;
        }

        if (destination.equals("A") && isRegister(source)) {
            registers.setACC(
                    registers.getR(getRegisterNumber(source))
            );
            return;
        }

        throw new IllegalArgumentException("Invalid MOV instruction.");
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
            value = parseNumber(operands[1].substring(1));
        } else if (isRegister(operands[1])) {
            value = registers.getR(getRegisterNumber(operands[1]));
        } else {
            throw new IllegalArgumentException("Invalid ADD operand.");
        }

        int result = registers.getACC() + value;

        flags.setCarry(result > 0xFF);
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
            value = parseNumber(operands[1].substring(1));
        } else if (isRegister(operands[1])) {
            value = registers.getR(getRegisterNumber(operands[1]));
        } else {
            throw new IllegalArgumentException("Invalid SUBB operand.");
        }

        int carry = flags.isCarry() ? 1 : 0;
        int result = registers.getACC() - value - carry;

        flags.setCarry(result < 0);
        registers.setACC(result);
    }

    // ============================================================
    // MUL AB
    // ============================================================

    private void executeMUL(String[] operands) {

        if (operands.length != 1 ||
                !operands[0].equalsIgnoreCase("AB")) {
            throw new IllegalArgumentException("MUL syntax: MUL AB");
        }

        int A = registers.getACC();
        int B = registers.getB();

        int result = A * B;

        registers.setACC(result & 0xFF);
        registers.setB((result >> 8) & 0xFF);

        flags.setCarry(false);
        flags.setOverflow(result > 0xFF);
    }

    // ============================================================
    // DIV AB
    // ============================================================

    private void executeDIV(String[] operands) {

        if (operands.length != 1 ||
                !operands[0].equalsIgnoreCase("AB")) {
            throw new IllegalArgumentException("DIV syntax: DIV AB");
        }

        int A = registers.getACC();
        int B = registers.getB();

        if (B == 0) {
            throw new ArithmeticException(
                    "Division by zero is not allowed."
            );
        }

        registers.setACC(A / B);
        registers.setB(A % B);

        flags.setCarry(false);
        flags.setOverflow(false);
    }

    // ============================================================
    // INC
    // ============================================================

    private void executeINC(String[] operands) {

        if (operands.length != 1) {
            throw new IllegalArgumentException("INC requires one operand.");
        }

        String operand = operands[0].toUpperCase();

        if (operand.equals("A")) {
            registers.setACC(registers.getACC() + 1);
        } else if (isRegister(operand)) {
            int number = getRegisterNumber(operand);
            registers.setR(number, registers.getR(number) + 1);
        } else {
            throw new IllegalArgumentException("Invalid INC operand.");
        }
    }

    // ============================================================
    // DEC
    // ============================================================

    private void executeDEC(String[] operands) {

        if (operands.length != 1) {
            throw new IllegalArgumentException("DEC requires one operand.");
        }

        String operand = operands[0].toUpperCase();

        if (operand.equals("A")) {
            registers.setACC(registers.getACC() - 1);
        } else if (isRegister(operand)) {
            int number = getRegisterNumber(operand);
            registers.setR(number, registers.getR(number) - 1);
        } else {
            throw new IllegalArgumentException("Invalid DEC operand.");
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
            value = parseNumber(operands[1].substring(1));
        } else if (isRegister(operands[1])) {
            value = registers.getR(getRegisterNumber(operands[1]));
        } else {
            throw new IllegalArgumentException("Invalid ANL operand.");
        }

        registers.setACC(registers.getACC() & value);
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
            value = parseNumber(operands[1].substring(1));
        } else if (isRegister(operands[1])) {
            value = registers.getR(getRegisterNumber(operands[1]));
        } else {
            throw new IllegalArgumentException("Invalid ORL operand.");
        }

        registers.setACC(registers.getACC() | value);
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
            value = parseNumber(operands[1].substring(1));
        } else if (isRegister(operands[1])) {
            value = registers.getR(getRegisterNumber(operands[1]));
        } else {
            throw new IllegalArgumentException("Invalid XRL operand.");
        }

        registers.setACC(registers.getACC() ^ value);
    }

    // ============================================================
    // CLR
    // ============================================================

    private void executeCLR(String[] operands) {

        if (operands.length != 1) {
            throw new IllegalArgumentException("CLR requires one operand.");
        }

        String operand = operands[0].toUpperCase();

        if (operand.equals("A")) {
            registers.setACC(0);
        } else if (operand.equals("C")) {
            flags.setCarry(false);
        } else {
            throw new IllegalArgumentException("CLR supports A or C.");
        }
    }

    // ============================================================
    // SJMP
    // ============================================================

    private void executeSJMP(String[] operands) {

        if (operands.length != 1) {
            throw new IllegalArgumentException("SJMP requires one operand.");
        }

        registers.setPC(parseNumber(operands[0]));
    }

    // ============================================================
    // JZ
    // ============================================================

    private void executeJZ(String[] operands) {

        requireOneAddressOperand("JZ", operands);

        if (registers.getACC() == 0) {
            registers.setPC(parseNumber(operands[0]));
        }
    }

    // ============================================================
    // CONDITIONAL JUMPS
    // ============================================================

    private void executeJNZ(String[] operands) {

        requireOneAddressOperand("JNZ", operands);

        if (registers.getACC() != 0) {
            registers.setPC(parseNumber(operands[0]));
        }
    }

    private void executeJC(String[] operands) {

        requireOneAddressOperand("JC", operands);

        if (flags.isCarry()) {
            registers.setPC(parseNumber(operands[0]));
        }
    }

    private void executeJNC(String[] operands) {

        requireOneAddressOperand("JNC", operands);

        if (!flags.isCarry()) {
            registers.setPC(parseNumber(operands[0]));
        }
    }

    private void requireOneAddressOperand(
            String mnemonic, String[] operands) {

        if (operands.length != 1) {
            throw new IllegalArgumentException(
                    mnemonic + " requires one address."
            );
        }
    }

    // ============================================================
    // DATA MEMORY ACCESS
    // ============================================================

    // LOAD A,address
    // LOAD A,[Rn]

    private void executeLOAD(String[] operands) {

        if (operands.length != 2 ||
                !operands[0].equalsIgnoreCase("A")) {
            throw new IllegalArgumentException(
                    "LOAD syntax: LOAD A,address or LOAD A,[Rn]"
            );
        }

        int address;

        if (isMemoryRegister(operands[1])) {

            address = registers.getR(
                    getMemoryRegisterNumber(operands[1])
            );

        } else {

            address = parseNumber(operands[1]);
        }

        registers.setACC(dataMemory.read(address));
    }

    // STORE address
    // STORE [Rn],A

    private void executeSTORE(String[] operands) {

        int address;

        // Direct addressing: STORE 00H
        if (operands.length == 1) {

            address = parseNumber(operands[0]);

        }
        // Indirect addressing: STORE [R0],A
        else if (operands.length == 2 &&
                isMemoryRegister(operands[0]) &&
                operands[1].equalsIgnoreCase("A")) {

            address = registers.getR(
                    getMemoryRegisterNumber(operands[0])
            );

        } else {

            throw new IllegalArgumentException(
                    "STORE syntax: STORE address or STORE [Rn],A"
            );
        }

        dataMemory.write(address, registers.getACC());
    }

    // ============================================================
    // ACCUMULATOR SHIFTS
    // ============================================================

    private void executeSHL(String[] operands) {

        if (operands.length != 1 ||
                !operands[0].equalsIgnoreCase("A")) {
            throw new IllegalArgumentException("SHL syntax: SHL A");
        }

        int value = registers.getACC();

        flags.setCarry((value & 0x80) != 0);

        registers.setACC((value << 1) & 0xFF);
    }

    private void executeSHR(String[] operands) {

        if (operands.length != 1 ||
                !operands[0].equalsIgnoreCase("A")) {
            throw new IllegalArgumentException("SHR syntax: SHR A");
        }

        int value = registers.getACC();

        flags.setCarry((value & 0x01) != 0);

        registers.setACC((value >>> 1) & 0xFF);
    }

    // ============================================================
    // PUSH
    // ============================================================

    private void executePUSH(String[] operands) {

        if (operands.length != 1) {
            throw new IllegalArgumentException(
                    "PUSH requires one register"
            );
        }

        String operand = operands[0].trim().toUpperCase();

        int value;

        if (operand.equals("A")) {
            value = registers.getACC();

        } else if (operand.equals("B")) {
            value = registers.getB();

        } else if (isRegister(operand)) {
            int number = getRegisterNumber(operand);
            value = registers.getR(number);

        } else {
            throw new IllegalArgumentException(
                    "Invalid PUSH register: " + operand
            );
        }

        stackPointer.push(dataMemory, value);
    }

    // ============================================================
    // POP
    // ============================================================

    private void executePOP(String[] operands) {

        if (operands.length != 1) {
            throw new IllegalArgumentException(
                    "POP requires one operand."
            );
        }

        String destination = operands[0].toUpperCase();

        int value = stackPointer.pop(dataMemory);

        if (destination.equals("A")) {
            registers.setACC(value);

        } else if (isRegister(destination)) {
            registers.setR(
                    getRegisterNumber(destination),
                    value
            );

        } else {
            throw new IllegalArgumentException(
                    "Invalid POP destination."
            );
        }
    }

    // ============================================================
    // ENQUEUE
    // ============================================================

    private void executeEnqueue() {

        String[] operands = decodedInstruction.getOperands();

        if (operands.length != 1) {
            throw new IllegalArgumentException(
                    "ENQ syntax: ENQ #data or ENQ A"
            );
        }

        String operand = operands[0].trim().toUpperCase();

        int value;

        if (operand.equals("A")) {

            value = registers.getACC();

        } else if (operand.startsWith("#")) {

            value = parseImmediate(operand);

        } else {

            throw new IllegalArgumentException(
                    "ENQ syntax: ENQ #data or ENQ A"
            );
        }

        if (value < 0 || value > 255) {
            throw new IllegalArgumentException(
                    "ENQ value must be between 0 and 255."
            );
        }

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

        int value = queue.dequeue();

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

        if (text == null || text.length() != 2) {
            return false;
        }

        char first = Character.toUpperCase(text.charAt(0));
        char second = text.charAt(1);

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

    private boolean isMemoryRegister(String text) {

        if (text == null) {
            return false;
        }

        String operand = text.trim().toUpperCase();

        return operand.matches("\\[R[0-7]\\]");
    }

    private int getMemoryRegisterNumber(String text) {

        if (!isMemoryRegister(text)) {
            throw new IllegalArgumentException(
                    "Invalid memory operand: " + text
            );
        }

        return getRegisterNumber(
                text.trim().substring(1, 3)
        );
    }

    // ============================================================
    // NUMBER PARSING
    // Supports decimal, 0x hex, and H-suffix hex
    // ============================================================

    private int parseNumber(String text) {

        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Number is missing."
            );
        }

        text = text.trim().toUpperCase();

        if (text.startsWith("0X")) {
            return Integer.parseInt(text.substring(2), 16);
        }

        if (text.endsWith("H")) {
            return Integer.parseInt(
                    text.substring(0, text.length() - 1), 16
            );
        }

        if (text.matches(".*[A-F].*")) {
            return Integer.parseInt(text, 16);
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

        operand = operand.trim();

        if (operand.startsWith("#")) {
            operand = operand.substring(1);
        }

        return parseNumber(operand);
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

        System.out.println(registers.toString());

        System.out.println("Flags = " + flags.toString());

        System.out.println(
                "SP = " + String.format(
                        "%02X", stackPointer.getValue()
                )
        );

        System.out.println("Queue = " + queue);

        System.out.println(
                "Queue Status = " + queue.getStatus()
        );

        System.out.println("Halted = " + halted);
    }
}
