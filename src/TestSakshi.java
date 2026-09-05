public class TestSakshi {

    public static void main(String[] args) {

        // Create memory
        Memory memory = new Memory();

        // Create Stack Pointer
        StackPointer sp = new StackPointer();

        // Create Status Flags
        StatusFlags flags = new StatusFlags();

        System.out.println("===== SAKSHI COMPONENT TEST =====");

        // Test Memory
        System.out.println("\n--- Memory Test ---");

        memory.write(0x20, 0x55);

        System.out.println(
            "Memory[20H] = "
            + String.format("%02X", memory.read(0x20))
        );

        // Test Stack Pointer
        System.out.println("\n--- Stack Pointer Test ---");

        System.out.println(
            "Initial SP = "
            + String.format("%02X", sp.getValue())
        );

        // PUSH
        sp.push(memory, 0x25);

        System.out.println(
            "After PUSH, SP = "
            + String.format("%02X", sp.getValue())
        );

        System.out.println(
            "Stack value = "
            + String.format("%02X", memory.read(sp.getValue()))
        );

        // POP
        int poppedValue = sp.pop(memory);

        System.out.println(
            "Popped value = "
            + String.format("%02X", poppedValue)
        );

        System.out.println(
            "After POP, SP = "
            + String.format("%02X", sp.getValue())
        );

        // Test Flags
        System.out.println("\n--- Status Flags Test ---");

        flags.setCarry(true);
        flags.setOverflow(true);

        System.out.println(flags);

        // Reset
        System.out.println("\n--- Reset Test ---");

        sp.reset();
        flags.reset();
        memory.reset();

        System.out.println(
            "SP after reset = "
            + String.format("%02X", sp.getValue())
        );

        System.out.println("Flags after reset = " + flags);

        System.out.println(
            "Memory[20H] after reset = "
            + String.format("%02X", memory.read(0x20))
        );

        System.out.println("\n===== TEST COMPLETE =====");
    }
}