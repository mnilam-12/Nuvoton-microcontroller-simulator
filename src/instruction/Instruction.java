package src.instruction;

public class Instruction {

    private String opcode;
    private int size;

    public Instruction(String opcode, int size) {
        this.opcode = opcode;
        this.size = size;
    }

    public String getOpcode() {
        return opcode;
    }

    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        return opcode;
    }
}