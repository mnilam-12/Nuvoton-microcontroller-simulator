package src.instruction;

import java.util.HashMap;
import java.util.Map;

public class InstructionSet {

    /*
     * Instruction categories for the
     * MS51FB9AE / 8051 instruction set.
     */
    private static final Map<String, String> CATEGORIES =
        new HashMap<>();

    static {

        // Data Transfer
        CATEGORIES.put("MOV", "Data Transfer");
        CATEGORIES.put("MOVX", "Data Transfer");
        CATEGORIES.put("MOVC", "Data Transfer");

        // Arithmetic
        CATEGORIES.put("ADD", "Arithmetic");
        CATEGORIES.put("SUB", "Arithmetic");
        CATEGORIES.put("INC", "Increment");
        CATEGORIES.put("DEC", "Decrement");
        CATEGORIES.put("MUL", "Arithmetic");
        CATEGORIES.put("DIV", "Arithmetic");

        // Logical
        CATEGORIES.put("ANL", "Logical");
        CATEGORIES.put("ORL", "Logical");
        CATEGORIES.put("XRL", "Logical");
        CATEGORIES.put("CPL", "Logical");

        // Branch
        CATEGORIES.put("SJMP", "Control Flow");
        CATEGORIES.put("LJMP", "Control Flow");
        CATEGORIES.put("CALL", "Control Flow");
        CATEGORIES.put("RET", "Control Flow");
        CATEGORIES.put("DJNZ", "Control Flow");

        // Bit Operations
        CATEGORIES.put("SETB", "Bit Operation");
        CATEGORIES.put("CLR", "Bit Operation");
        CATEGORIES.put("JB", "Bit Operation");
        CATEGORIES.put("JNB", "Bit Operation");

        // Simulator termination
        CATEGORIES.put("HALT", "Program Termination");
    }


    // ==========================================
    // DECODE
    // ==========================================
public static DecodedInstruction decode(
            Instruction instruction) {

        if (instruction == null) {
            throw new IllegalArgumentException(
                "Instruction cannot be null."
            );
        }

        String text =
            instruction.getText().trim();

        if (text.isEmpty()) {
            throw new IllegalArgumentException(
                "Instruction text cannot be empty."
            );
        }

        /*
         * Separate mnemonic from operands.
         *
         * Example:
         *
         * MOV A, #05
         *
         * mnemonic = MOV
         * operands = A, #05
         */

        String[] parts =
            text.split("\\s+", 2);

        String mnemonic =
            parts[0].toUpperCase();

        // Check whether instruction is known
        if (!CATEGORIES.containsKey(mnemonic)) {

            throw new IllegalArgumentException(
                "Unknown instruction: " + mnemonic
            );
        }

        String operandText = "";

        if (parts.length > 1) {
            operandText = parts[1].trim();
        }

        String[] operands =
            parseOperands(operandText);

        return new DecodedInstruction(
            mnemonic,
            operands
            );
    }


    // ==========================================
    // PARSE OPERANDS
    // ==========================================

    private static String[] parseOperands(
            String operandText) {

        if (operandText == null
                || operandText.trim().isEmpty()) {

            return new String[0];
        }

        String[] operands =
            operandText.split(",");

        for (int i = 0; i < operands.length; i++) {

            operands[i] =
                operands[i].trim().toUpperCase();
        }

        return operands;
    }


    // ==========================================
    // CHECK INSTRUCTION
    // ==========================================

    public static boolean isSupported(
            String mnemonic) {

        if (mnemonic == null) {
            return false;
        }

        return CATEGORIES.containsKey(
            mnemonic.trim().toUpperCase()
        );
    }


    // ==========================================
    // GET CATEGORY
    // ==========================================

    public static String getCategory(
            String mnemonic) {

        if (mnemonic == null) {
            return null;
                  }

        return CATEGORIES.get(
            mnemonic.trim().toUpperCase()
        );
    }


    // ==========================================
    // DISPLAY INSTRUCTION INFORMATION
    // ==========================================

    public static String getInstructionInfo(
            String mnemonic) {

        if (!isSupported(mnemonic)) {

            return "Unsupported instruction: "
                + mnemonic;
        }

        String name =
            mnemonic.trim().toUpperCase();

        return "Instruction: " + name
            + "\nCategory: " + CATEGORIES.get(name);
    }
}
