package src.queue;

public class FIFOQueue {
     // Maximum number of elements in queue
    private static final int CAPACITY = 8;
    //array used to store the elements in the queue
    private final int[] queue;
    //front position
    private int front;
    //rear position
    private int rear;
    // no of elements in currently in the queue
    private int size;


    //.............................................
    //  constructor................................
    public FIFOQueue(){
        queue=new int[CAPACITY];
            reset();
        
    }
    //........................ENQUEUE...........
    //adding the element to the rear position
    public void enqueue(int value){
    if(isFull()){
        throw new IllegalStateException(
            "Queue is full"
        );
    }
        queue[rear]=value & 0xFF;
        rear=(rear+1)%CAPACITY;
        size++;

    
}

////dequeue..........
/// removing an element from the front
public int dequeue(){
    if(isEmpty()){
        throw new IllegalStateException(
            "Queue is empty"
        );
    }
     int value = queue[front];

        // Optional: clear removed position
        queue[front] = 0;

        front = (front + 1) % CAPACITY;

        size--;

        return value;
    }

///.................checking whether the Queue is empty........
public boolean isEmpty(){
    return size ==0;
}

//..................checking whether the Queue is full..............
public boolean isFull(){
    return size ==CAPACITY;
} 
//....................get Queue size...........
public int size(){
     return size;
}
///.............get queue capacity.............
public int getCapacity(){
    return  CAPACITY;
}
////get front index........
public int getFront(){
    return front;
}
///...........get rear index.............
public int getRear(){
    return rear;
}
// ============================================================
    // GET VALUE AT A PHYSICAL QUEUE INDEX
    // ============================================================

    public int getValue(int index) {

        if (index < 0 || index >= CAPACITY) {

            throw new IllegalArgumentException(
                    "Invalid queue index: " + index
            );
        }

        return queue[index];
    }
    // ============================================================
    // RESET QUEUE
    // ============================================================

    public void reset() {

        front = 0;

        rear = 0;

        size = 0;

        for (int i = 0; i < CAPACITY; i++) {

            queue[i] = 0;
        }
    }
    // ============================================================
    // GET QUEUE STATUS
    // ============================================================

    public String getStatus() {

        if (isEmpty()) {

            return "EMPTY";
        }

        if (isFull()) {

            return "FULL";
        }

        return "READY";
    }
     // ============================================================
    // DISPLAY QUEUE
    // ============================================================

    @Override
    public String toString() {

        if (isEmpty()) {

            return "[ EMPTY ]";
        }

        StringBuilder result =
                new StringBuilder();

        result.append("[ ");

        for (int i = 0; i < size; i++) {

            int index =
                    (front + i) % CAPACITY;

            result.append(
                    String.format(
                            "%02X",
                            queue[index]
                    )
            );

            if (i < size - 1) {

                result.append(" | ");
            }
        }

        result.append(" ]");

        return result.toString();
    }
}

