package src;

import src.cpu.CPU;

public class QueueCPUTest {

    public static void main(String[] args) {

        CPU cpu = new CPU();

        System.out.println("===== CPU QUEUE TEST =====");

        String program =
                "ENQ #10\n" +
                "ENQ #20\n" +
                "ENQ #30\n" +
                "DEQ\n" +
                "DEQ\n" +
                "HALT";

        System.out.println("\nLoading program...");
        cpu.loadProgram(program);

        System.out.println("Program loaded successfully.");

        System.out.println("\n===== EXECUTION =====");

        while (!cpu.isHalted()) {

            cpu.step();

            System.out.println("\nPC  = " +
                    String.format("%04X", cpu.getRegisters().getPC()));

            System.out.println("ACC = " +
                    String.format("%02X", cpu.getRegisters().getACC()));

            System.out.println("Queue = " + cpu.getQueue());

            System.out.println("Queue Status = " +
                    cpu.getQueue().getStatus());
        }

        System.out.println("\n===== FINAL RESULT =====");

        System.out.println("ACC = " +
                String.format("%02X", cpu.getRegisters().getACC()));

        System.out.println("Queue = " + cpu.getQueue());

        System.out.println("Queue Size = " +
                cpu.getQueue().size());

        System.out.println("CPU Halted = " +
                cpu.isHalted());

        System.out.println("\n===== TEST COMPLETED =====");
    }
}