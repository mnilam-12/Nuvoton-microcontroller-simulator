package src.memory;

import src.instruction.Instruction;

public class ProgramMemory {

    private static final int MEMORY_SIZE = 256;

    private Instruction[] memory;

    public ProgramMemory() {
        memory = new Instruction[MEMORY_SIZE];
        reset();
    }

    public void load(int address, Instruction instruction) {
        checkAddress(address);
        memory[address] = instruction;
    }

    public Instruction read(int address) {
        checkAddress(address);
        return memory[address];
    }

    public void reset() {
        for (int i = 0; i < MEMORY_SIZE; i++) {
            memory[i] = null;
        }
    }

    public int getSize() {
        return MEMORY_SIZE;
    }

    private void checkAddress(int address) {
        if (address < 0 || address >= MEMORY_SIZE) {
            throw new IllegalArgumentException(
                "Invalid program memory address: " + address
            );
        }
    }
}