package src.memory;

public class Memory {

    private static final int MEMORY_SIZE = 256;

    private final int[] memory;

    public Memory() {
        memory = new int[MEMORY_SIZE];
        reset();
    }

    public void write(int address, int value) {
        checkAddress(address);
        memory[address] = value & 0xFF;
    }

    public int read(int address) {
        checkAddress(address);
        return memory[address];
    }

    public void reset() {
        for (int i = 0; i < MEMORY_SIZE; i++) {
            memory[i] = 0;
        }
    }

    public int getSize() {
        return MEMORY_SIZE;
    }

    private void checkAddress(int address) {
        if (address < 0 || address >= MEMORY_SIZE) {
            throw new IllegalArgumentException(
                "Invalid memory address: " + address
            );
        }
    }
}