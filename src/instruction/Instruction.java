package src.instruction;

public class Instruction {

    private String text;
    private int size;

    // Constructor
    public Instruction(String text, int size) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Instruction cannot be empty."
            );
        }

        if (size <= 0) {
            throw new IllegalArgumentException(
                "Instruction size must be greater than 0."
            );
        }

        this.text = text.trim();
        this.size = size;
    }

    // Return complete instruction text
    public String getText() {
        return text;
    }

    // Return instruction size
    public int getSize() {
        return size;
    }

    // Return instruction as text
    @Override
    public String toString() {
        return text;
    }
}
