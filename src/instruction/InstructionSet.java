package src.instruction;

public class InstructionSet {

    public static DecodedInstruction decode(Instruction instruction) {

        String text = instruction.getOpcode().trim();

        if (text.isEmpty()) {
            throw new IllegalArgumentException("Empty instruction");
        }

        String[] parts = text.split("\\s+", 2);

        String mnemonic = parts[0].toUpperCase();

        String[] operands = new String[0];

        if (parts.length > 1) {
            operands = parts[1].split("\\s*,\\s*");
        }

        switch (mnemonic) {

            // Data transfer
            case "MOV":

            // Arithmetic
            case "ADD":
            case "SUBB":
            case "MUL":
            case "DIV":

            // Logical
            case "ANL":
            case "ORL":
            case "XRL":
            case "CLR":

            // Increment / decrement
            case "INC":
            case "DEC":

            // Control flow
            case "SJMP":

            // Stack
            case "PUSH":
            case "POP":

            // FIFO Queue
            case "ENQ":
            case "DEQ":

            // Termination
            case "HALT":
                break;

            default:
                throw new IllegalArgumentException(
                    "Unknown instruction: " + mnemonic
                );
        }

        return new DecodedInstruction(mnemonic, operands);
    }
}