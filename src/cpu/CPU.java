package src.cpu;

import src.memory.Memory;
import src.memory.ProgramMemory;
import src.instruction.Instruction;
import src.instruction.DecodedInstruction;
import src.instruction.InstructionSet;

public class CPU {

    private Registers registers;
    private StatusFlags flags;
    private StackPointer stackPointer;

    private Memory dataMemory;
    private ProgramMemory programMemory;

    private Instruction currentInstruction;
    private DecodedInstruction decodedInstruction;

    private boolean halted;

    public CPU() {
        registers = new Registers();
        flags = new StatusFlags();
        stackPointer = new StackPointer();

        dataMemory = new Memory();
        programMemory = new ProgramMemory();

        reset();
    }

    public void reset() {

        registers.reset();
        flags.reset();
        stackPointer.reset();

        dataMemory.reset();
        programMemory.reset();

        currentInstruction = null;
        decodedInstruction = null;

        halted = false;
    }

    public void loadProgram() {

        programMemory.reset();

        programMemory.load(
            0,
            new Instruction("MOV A, #05", 2)
        );

        programMemory.load(
            2,
            new Instruction("MOV R1, #03", 2)
        );

        programMemory.load(
            4,
            new Instruction("ADD A, R1", 1)
        );

        programMemory.load(
            5,
            new Instruction("INC A", 1)
        );

        programMemory.load(
            6,
            new Instruction("ANL A, #0F", 2)
        );

        programMemory.load(
            8,
            new Instruction("DEC A", 1)
        );

        programMemory.load(
            9,
            new Instruction("SJMP 0B", 2)
        );

        programMemory.load(
            11,
            new Instruction("HALT", 1)
        );

        registers.setPC(0);

        halted = false;
    }

    public Instruction fetch() {

        int pc = registers.getPC();

        Instruction instruction = programMemory.read(pc);

        if (instruction == null) {

            throw new IllegalStateException(
                "No instruction at program memory address: "
                + String.format("%04X", pc)
            );
        }

        currentInstruction = instruction;

        // Move PC to the next instruction
        registers.setPC(
            pc + instruction.getSize()
        );

        return instruction;
    }

    public DecodedInstruction decode() {

        if (currentInstruction == null) {

            throw new IllegalStateException(
                "No instruction available for decoding."
            );
        }

        decodedInstruction =
            InstructionSet.decode(currentInstruction);

        return decodedInstruction;
    }

    public void execute() {

        if (decodedInstruction == null) {

            throw new IllegalStateException(
                "No decoded instruction available."
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

            case "INC":
                executeINC(operands);
                break;

            case "DEC":
                executeDEC(operands);
                break;

            case "ANL":
                executeANL(operands);
                break;

            case "SJMP":
                executeSJMP(operands);
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

        // MOV A, #data
        if (destination.equals("A")
                && source.startsWith("#")) {

            int value =
                parseNumber(source.substring(1));

            registers.setACC(value);

            updateZeroFlag();
        }

        // MOV Rn, #data
        else if (destination.matches("R[0-7]")
                && source.startsWith("#")) {

            int registerNumber =
                Integer.parseInt(
                    destination.substring(1)
                );

            int value =
                parseNumber(source.substring(1));

            registers.setR(
                registerNumber,
                value
            );
        }

        // MOV Rn, A
        else if (destination.matches("R[0-7]")
                && source.equals("A")) {

            int registerNumber =
                Integer.parseInt(
                    destination.substring(1)
                );

            registers.setR(
                registerNumber,
                registers.getACC()
            );
        }

        // MOV A, Rn
        else if (destination.equals("A")
                && source.matches("R[0-7]")) {

            int registerNumber =
                Integer.parseInt(
                    source.substring(1)
                );

            registers.setACC(
                registers.getR(registerNumber)
            );

            updateZeroFlag();
        }

        else {

            throw new IllegalArgumentException(
                "Unsupported MOV operation."
            );
        }
    }

    private void executeADD(String[] operands) {

        if (operands.length != 2
                || !operands[0].equalsIgnoreCase("A")) {

            throw new IllegalArgumentException(
                "Supported form: ADD A, operand"
            );
        }

        int value;

        String source =
            operands[1].toUpperCase();

        // ADD A, #data
        if (source.startsWith("#")) {

            value =
                parseNumber(source.substring(1));
        }

        // ADD A, Rn
        else if (source.matches("R[0-7]")) {

            int registerNumber =
                Integer.parseInt(
                    source.substring(1)
                );

            value =
                registers.getR(registerNumber);
        }

        else {

            throw new IllegalArgumentException(
                "Invalid ADD operand."
            );
        }

        int result =
            registers.getACC() + value;

        // Carry flag
        flags.setCarry(result > 0xFF);

        registers.setACC(result);

        // Zero flag
        updateZeroFlag();
    }

    private void executeINC(String[] operands) {

        if (operands.length != 1) {

            throw new IllegalArgumentException(
                "INC requires one operand."
            );
        }

        String operand =
            operands[0].toUpperCase();

        // INC A
        if (operand.equals("A")) {

            registers.setACC(
                registers.getACC() + 1
            );

            updateZeroFlag();
        }

        // INC Rn
        else if (operand.matches("R[0-7]")) {

            int registerNumber =
                Integer.parseInt(
                    operand.substring(1)
                );

            registers.setR(
                registerNumber,
                registers.getR(registerNumber) + 1
            );
        }

        else {

            throw new IllegalArgumentException(
                "Invalid INC operand."
            );
        }
    }

    private void executeDEC(String[] operands) {

        if (operands.length != 1) {

            throw new IllegalArgumentException(
                "DEC requires one operand."
            );
        }

        String operand =
            operands[0].toUpperCase();

        // DEC A
        if (operand.equals("A")) {

            registers.setACC(
                registers.getACC() - 1
            );

            updateZeroFlag();
        }

        // DEC Rn
        else if (operand.matches("R[0-7]")) {

            int registerNumber =
                Integer.parseInt(
                    operand.substring(1)
                );

            registers.setR(
                registerNumber,
                registers.getR(registerNumber) - 1
            );
        }

        else {

            throw new IllegalArgumentException(
                "Invalid DEC operand."
            );
        }
    }

    private void executeANL(String[] operands) {

        if (operands.length != 2
                || !operands[0].equalsIgnoreCase("A")) {

            throw new IllegalArgumentException(
                "Supported form: ANL A, operand"
            );
        }

        int value;

        String source =
            operands[1].toUpperCase();

        // ANL A, #data
        if (source.startsWith("#")) {

            value =
                parseNumber(source.substring(1));
        }

        // ANL A, Rn
        else if (source.matches("R[0-7]")) {

            int registerNumber =
                Integer.parseInt(
                    source.substring(1)
                );

            value =
                registers.getR(registerNumber);
        }

        else {

            throw new IllegalArgumentException(
                "Invalid ANL operand."
            );
        }

        registers.setACC(
            registers.getACC() & value
        );

        updateZeroFlag();
    }

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

    public void step() {

        if (halted) {
            return;
        }

        fetch();

        decode();

        execute();
    }

    // --------------------------------------------------
    // ZERO FLAG
    // --------------------------------------------------

    private void updateZeroFlag() {

        flags.setZero(
            registers.getACC() == 0
        );
    }

    // --------------------------------------------------
    // GETTERS
    // --------------------------------------------------

    public Registers getRegisters() {
        return registers;
    }

    public StatusFlags getFlags() {
        return flags;
    }

    public StackPointer getStackPointer() {
        return stackPointer;
    }

    public Instruction getCurrentInstruction() {
        return currentInstruction;
    }

    public DecodedInstruction getDecodedInstruction() {
        return decodedInstruction;
    }

    public boolean isHalted() {
        return halted;
    }

    public Memory getDataMemory() {
        return dataMemory;
    }

    public ProgramMemory getProgramMemory() {
        return programMemory;
    }

    // --------------------------------------------------
    // NUMBER PARSER
    // --------------------------------------------------

    private int parseNumber(String text) {

        text = text.trim();

        // Hexadecimal with 0x prefix
        if (text.startsWith("0x")
                || text.startsWith("0X")) {

            return Integer.parseInt(
                text.substring(2),
                16
            );
        }

        // Hexadecimal such as 0F, 05, FF
        if (text.matches(".*[A-Fa-f].*")) {

            return Integer.parseInt(
                text,
                16
            );
        }

        // Decimal number
        return Integer.parseInt(text);
    }
}
