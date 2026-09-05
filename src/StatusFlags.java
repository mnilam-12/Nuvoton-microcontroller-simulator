public class StatusFlags {

    // Carry Flag
    private boolean carry;

    // Auxiliary Carry Flag
    private boolean auxiliaryCarry;

    // Overflow Flag
    private boolean overflow;

    // Parity Flag
    private boolean parity;

    // Flag 0
    private boolean flag0;

    // Flag 1
    private boolean flag1;

    // Constructor
    public StatusFlags() {
        reset();
    }

    // Carry Flag
    public boolean isCarry() {
        return carry;
    }

    public void setCarry(boolean carry) {
        this.carry = carry;
    }

    // Auxiliary Carry Flag
    public boolean isAuxiliaryCarry() {
        return auxiliaryCarry;
    }

    public void setAuxiliaryCarry(boolean auxiliaryCarry) {
        this.auxiliaryCarry = auxiliaryCarry;
    }

    // Overflow Flag
    public boolean isOverflow() {
        return overflow;
    }

    public void setOverflow(boolean overflow) {
        this.overflow = overflow;
    }

    // Parity Flag
    public boolean isParity() {
        return parity;
    }

    public void setParity(boolean parity) {
        this.parity = parity;
    }

    // Flag 0
    public boolean isFlag0() {
        return flag0;
    }

    public void setFlag0(boolean flag0) {
        this.flag0 = flag0;
    }

    // Flag 1
    public boolean isFlag1() {
        return flag1;
    }

    public void setFlag1(boolean flag1) {
        this.flag1 = flag1;
    }

    // Reset all flags
    public void reset() {
        carry = false;
        auxiliaryCarry = false;
        overflow = false;
        parity = false;
        flag0 = false;
        flag1 = false;
    }

    // Display flags
    @Override
    public String toString() {
        return "CY=" + (carry ? 1 : 0)
                + " AC=" + (auxiliaryCarry ? 1 : 0)
                + " OV=" + (overflow ? 1 : 0)
                + " P=" + (parity ? 1 : 0)
                + " F0=" + (flag0 ? 1 : 0)
                + " F1=" + (flag1 ? 1 : 0);
    }
}