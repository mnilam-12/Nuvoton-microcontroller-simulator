package src.instruction;

public class DecodedInstruction {

    private String mnemonic;
    private String[] operands;

    // Constructor
    public DecodedInstruction(
            String mnemonic,
            String[] operands) {

        if (mnemonic == null || mnemonic.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Mnemonic cannot be empty."
            );
        }

        this.mnemonic = mnemonic.trim().toUpperCase();

        if (operands == null) {
            this.operands = new String[0];
        } else {
            this.operands = operands;
        }
    }

    // Get instruction mnemonic
    public String getMnemonic() {
        return mnemonic;
    }

    // Get instruction operands
    public String[] getOperands() {
        return operands;
    }

    // Display decoded instruction
    @Override
    public String toString() {

        if (operands.length == 0) {
            return mnemonic;
        }

        StringBuilder result =
            new StringBuilder(mnemonic);

        result.append(" ");

        for (int i = 0; i < operands.length; i++) {

            result.append(operands[i]);

            if (i < operands.length - 1) {
                result.append(", ");
            }
        }

        return result.toString();
    }
}
