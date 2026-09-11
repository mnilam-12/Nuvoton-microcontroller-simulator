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

            case "MOV":
            case "ADD":
            case "SUBB":

            case "MUL":
            case "DIV":

            case "ANL":
            case "ORL":
            case "XRL":

            case "INC":
            case "DEC":

            case "CLR":

            case "SJMP":

            case "ENQ":
            case "DEQ":

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