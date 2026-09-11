package src;

import src.queue.FIFOQueue;

public class QueueTest {

    public static void main(String[] args) {

        FIFOQueue queue = new FIFOQueue();


        System.out.println("Initial Queue:");
        System.out.println(queue);

        System.out.println(
                "\nStatus: " + queue.getStatus()
        );


        // Enqueue values

        System.out.println("\nEnqueue 10");
        queue.enqueue(10);

        System.out.println("Enqueue 20");
        queue.enqueue(20);

        System.out.println("Enqueue 30");
        queue.enqueue(30);


        System.out.println(
                "\nQueue after enqueue:"
        );

        System.out.println(queue);

        System.out.println(
                "Size: " + queue.size()
        );


        // Dequeue values

        System.out.println("\nDequeue:");

        int value1 = queue.dequeue();

        System.out.println(
                "Removed: " + value1
        );


        System.out.println("\nQueue:");

        System.out.println(queue);


        int value2 = queue.dequeue();

        System.out.println(
                "Removed: " + value2
        );


        System.out.println("\nQueue:");

        System.out.println(queue);


        System.out.println(
                "\nFinal Status: "
                        + queue.getStatus()
        );
    }
}