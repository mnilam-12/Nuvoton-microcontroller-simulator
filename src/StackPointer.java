public class StackPointer {

    // Stack Pointer is an 8-bit register
    private int value;

    // MS51/8051 reset value of SP
    private static final int RESET_VALUE = 0x07;

    // Constructor
    public StackPointer() {
        reset();
    }

    // Get current stack pointer value
    public int getValue() {
        return value;
    }

    // Set stack pointer value
    public void setValue(int value) {
        this.value = value & 0xFF;
    }

    // Increment Stack Pointer
    public void increment() {
        value = (value + 1) & 0xFF;
    }

    // Decrement Stack Pointer
    public void decrement() {
        value = (value - 1) & 0xFF;
    }

    // Reset Stack Pointer
    public void reset() {
        value = RESET_VALUE;
    }

    // Push a value onto the stack
    public void push(Memory memory, int data) {
        increment();
        memory.write(value, data);
    }

    // Pop a value from the stack
    public int pop(Memory memory) {
        int data = memory.read(value);
        decrement();
        return data;
    }
}