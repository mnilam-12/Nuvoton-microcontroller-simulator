package src.instruction;

public class DecodedInstruction {

    private String mnemonic;
    private String[] operands;

    public DecodedInstruction(String mnemonic, String[] operands) {
        this.mnemonic = mnemonic;
        this.operands = operands;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public String[] getOperands() {
        return operands;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append(mnemonic);

        if (operands != null && operands.length > 0) {
            result.append(" ");

            for (int i = 0; i < operands.length; i++) {
                result.append(operands[i]);

                if (i < operands.length - 1) {
                    result.append(", ");
                }
            }
        }

        return result.toString();
    }
}