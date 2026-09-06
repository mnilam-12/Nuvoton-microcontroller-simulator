package src.cpu;

public class Registers {

    // =========================
    // PROGRAM COUNTER (PC)
    // =========================
    // PC is a 16-bit register.
    // It stores the address of the next instruction.

    private int PC;


    // =========================
    // ACCUMULATOR (A)
    // =========================
    // A is an 8-bit register.
    // It is commonly used for arithmetic and logical operations.

    private int ACC;


    // =========================
    // B REGISTER
    // =========================
    // B is an 8-bit register.

    private int B;


    // =========================
    // PROGRAM STATUS WORD
    // =========================
    // PSW is an 8-bit status register.

    private int PSW;


    // =========================
    // DATA POINTER (DPTR)
    // =========================
    // DPTR is a 16-bit register.

    private int DPTR;


    // =========================
    // GENERAL PURPOSE REGISTERS
    // =========================
    // R0 - R7 are eight 8-bit registers.

    private int[] R;


    // =========================
    // CONSTRUCTOR
    // =========================

    public Registers() {

        R = new int[8];

        reset();
    }


    // =========================
    // PROGRAM COUNTER
    // =========================

    public int getPC() {
        return PC;
    }

    public void setPC(int value) {
        PC = value & 0xFFFF;
    }


    // =========================
    // ACCUMULATOR
    // =========================

    public int getACC() {
        return ACC;
    }

    public void setACC(int value) {
        ACC = value & 0xFF;
    }


    // =========================
    // B REGISTER
    // =========================

    public int getB() {
        return B;
    }

    public void setB(int value) {
        B = value & 0xFF;
    }


    // =========================
    // PSW
    // =========================

    public int getPSW() {
        return PSW;
    }

    public void setPSW(int value) {
        PSW = value & 0xFF;
    }


    // =========================
    // DATA POINTER
    // =========================

    public int getDPTR() {
        return DPTR;
    }

    public void setDPTR(int value) {
        DPTR = value & 0xFFFF;
    }


    // =========================
    // R0 - R7
    // =========================

    public int getR(int index) {

        checkRegisterIndex(index);

        return R[index];
    }

    public void setR(int index, int value) {

        checkRegisterIndex(index);

        R[index] = value & 0xFF;
    }


    // =========================
    // REGISTER INDEX CHECK
    // =========================

    private void checkRegisterIndex(int index) {

        if (index < 0 || index > 7) {

            throw new IllegalArgumentException(
                "Invalid register index: R" + index
            );
        }
    }


    // =========================
    // RESET ALL REGISTERS
    // =========================

    public void reset() {

        PC = 0x0000;

        ACC = 0x00;

        B = 0x00;

        PSW = 0x00;

        DPTR = 0x0000;

        for (int i = 0; i < 8; i++) {

            R[i] = 0x00;
        }
    }


    // =========================
    // DISPLAY REGISTERS
    // =========================

    @Override
    public String toString() {

        StringBuilder result = new StringBuilder();

        result.append("PC   = ")
              .append(String.format("%04X", PC))
              .append("\n");

        result.append("ACC  = ")
              .append(String.format("%02X", ACC))
              .append("\n");

        result.append("B    = ")
              .append(String.format("%02X", B))
              .append("\n");

        result.append("PSW  = ")
              .append(String.format("%02X", PSW))
              .append("\n");

        result.append("DPTR = ")
              .append(String.format("%04X", DPTR))
              .append("\n");

        for (int i = 0; i < 8; i++) {

            result.append("R")
                  .append(i)
                  .append("   = ")
                  .append(String.format("%02X", R[i]))
                  .append("\n");
        }

        return result.toString();
    }
}