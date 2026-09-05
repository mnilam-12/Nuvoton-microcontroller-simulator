public class Memory {

    // MS51/8051-style internal data memory
    private static final int MEMORY_SIZE = 256;

    private final int[] memory;

    // Constructor
    public Memory() {
        memory = new int[MEMORY_SIZE];
        reset();
    }

    // Write an 8-bit value to memory
    public void write(int address, int value) {
        checkAddress(address);

        // Keep value within 8 bits (00H - FFH)
        memory[address] = value & 0xFF;
    }

    // Read a value from memory
    public int read(int address) {
        checkAddress(address);

        return memory[address];
    }

    // Reset all memory locations to 00H
    public void reset() {
        for (int i = 0; i < MEMORY_SIZE; i++) {
            memory[i] = 0;
        }
    }

    // Return memory size
    public int getSize() {
        return MEMORY_SIZE;
    }

    // Check whether the address is valid
    private void checkAddress(int address) {
        if (address < 0 || address >= MEMORY_SIZE) {
            throw new IllegalArgumentException(
                "Invalid memory address: " + address
            );
        }
    }
}