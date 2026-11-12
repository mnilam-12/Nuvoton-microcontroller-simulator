# Nuvoton MS51FB9AE Microcontroller Simulator

**Project Title: Educational Microcontroller Simulator with Process Scheduling**

## 1. About the Project

Our project is a Java-based educational simulator of the Nuvoton MS51FB9AE 8-bit microcontroller.

The main idea behind this project is to help students understand how a microcontroller works internally. Instead of learning everything only through theory, users can see how instructions are executed, how memory changes, and how different components work together.

The simulator comes with a graphical user interface where users can load programs, execute them step by step, run them, and reset the simulation. It also displays CPU registers, memory, stack, FIFO queue, and execution details.

We have also implemented communication between the User Interface, Core Simulator, and Logger using Inter-Process Communication (IPC). Hamming (7,4) code is included to demonstrate single-bit error detection.

## 2. Problem Statement

Understanding the internal working of a microcontroller can be difficult when learning only from textbooks.

To make this easier, we developed a software-based simulator that demonstrates the basic working of the Nuvoton MS51FB9AE microcontroller.

The project focuses on instruction execution, CPU operations, memory, stack, queue handling, and communication between different components. It also provides a graphical interface so that users can easily observe what happens during program execution.

## 3. Objectives

The main objectives of our project are:

- To understand the basic architecture of the Nuvoton MS51FB9AE microcontroller.
- To implement CPU registers and instruction execution.
- To demonstrate the Fetch–Decode–Execute cycle.
- To perform memory read and write operations.
- To implement stack operations using the Stack Pointer.
- To implement FIFO queue operations.
- To create a simple graphical interface using Java Swing.
- To establish communication between the UI, Core, and Logger.
- To record simulator activities using a logging system.
- To implement Hamming (7,4) code for error detection.

## 4. Microcontroller Used

**Nuvoton MS51FB9AE – 8-bit 8051-based Microcontroller**

Our simulator is based on the 8051 architecture. It represents selected features of the microcontroller, such as CPU registers, memory, stack, and instruction execution.

The project is an educational simulation and does not attempt to reproduce the complete physical microcontroller.

## 5. Technologies Used

| Technology | Purpose |
|---|---|
| Java | Main programming language |
| Java Swing | Developing the user interface |
| Visual Studio Code | Writing and running the code |
| Git | Managing code changes |
| GitHub | Team collaboration and project storage |
| Blocking Queues | Communication between components |
| Java Sockets | Logger communication |

## 6. System Architecture

Our simulator is divided into three main components.

### User Interface (UI)

The UI is the part that users interact with. It allows users to load programs, reset the simulator, execute instructions one at a time, and run the program.

It also displays CPU registers, memory, stack, FIFO queue, and execution messages.

### Core Simulator

The Core handles the actual instruction execution. It fetches, decodes, and executes instructions and updates the CPU registers and memory according to the operations performed.

It also handles stack and FIFO queue operations.

### Logger

The Logger keeps track of important activities happening inside the simulator. It receives messages, handles INFO, WARNING, and ERROR types, and saves them in a log file with timestamps.

### Basic Architecture

```text
          NUVOTON MS51FB9AE SIMULATOR
                       |
                 USER INTERFACE
                       |
                 IPC COMMUNICATION
                       |
                 CORE SIMULATOR
                       |
          +------------+------------+
          |            |            |
         CPU          MEMORY       STACK
          |            |            |
          +------------+------------+
                       |
                   FIFO QUEUE
                       |
                 LOGGER PROCESS
                       |
                    LOG FILE
```

## 7. Features Implemented

### 7.1 CPU and Instruction Execution

- Basic CPU register handling.
- Implementation of the Fetch–Decode–Execute cycle.
- Fetching instructions using the Program Counter.
- Decoding and executing instructions.
- Register updates during execution.
- Basic arithmetic and logical operations.
- Memory access operations.

### 7.2 Memory Management

- Program memory and data memory simulation.
- Memory read and write operations.
- Stack Pointer management.
- Monitoring memory contents.
- Updating CPU status flags.

### 7.3 Stack Operations

The simulator supports basic stack operations.

- PUSH is used to store data in the stack.
- POP is used to retrieve data from the stack.
- The Stack Pointer is updated during these operations.
- Stack contents can be monitored through the interface.

### 7.4 FIFO Queue

A FIFO queue is implemented to demonstrate how elements are inserted and removed in First-In-First-Out order.

- ENQUEUE inserts an element into the queue.
- DEQUEUE removes an element from the queue.
- Queue elements and their status can be observed during execution.

### 7.5 Graphical User Interface

We developed the simulator interface using Java Swing.

The interface includes:

- Program input area.
- LOAD button to load a program.
- RESET button to reset the simulator.
- STEP button to execute one instruction at a time.
- RUN button to execute the program.
- CPU register display.
- Program Counter display.
- Memory and stack display.
- FIFO queue display.
- Execution status and logging area.

### 7.6 Multi-Process Communication (IPC)

One of the important parts of our project is communication between the UI, Core Simulator, and Logger.

The communication works as follows:

- The UI sends commands such as LOAD, RESET, STEP, and RUN to the Core.
- The Core sends execution updates back to the UI.
- Important activities and messages are forwarded to the Logger.
- The Logger stores the received messages in a log file.

Blocking queues and socket-based communication are used to transfer messages between the components.

### 7.7 Logging System

The Logger helps us keep track of what happens during simulator execution.

It includes:

- INFO messages.
- WARNING messages.
- ERROR messages.
- Timestamped execution records.
- Storage of messages in `simulator.log`.

This makes it easier to monitor simulator activities and identify issues.

### 7.8 Hamming (7,4) Error Detection

We also implemented Hamming (7,4) code to demonstrate how a single-bit error can be detected.

The implementation includes:

- Taking a 4-bit binary input.
- Calculating three parity bits.
- Generating a 7-bit codeword.
- Changing a bit to introduce an error.
- Performing parity checks.
- Identifying the position of the incorrect bit using the syndrome.

**Example test result:**

| Description | Value |
|---|---|
| Original input | 1011 |
| Generated codeword | 0110011 |
| Received codeword | 0110001 |
| Error position | Bit 6 |
| Test status | PASS |

## 8. Testing and Results

We performed different test cases to check whether the implemented features were working correctly.

| Test ID | Test Description | Result |
|---|---|---|
| TC-01 | Fetch–Decode–Execute cycle | PASS |
| TC-02 | PUSH and POP operations | PASS |
| TC-03 | FIFO ENQUEUE and DEQUEUE | PASS |
| TC-04 | IPC message delivery | PASS |
| TC-05 | Hamming (7,4) error detection | PASS |

All five documented test cases passed during functional testing.

## 9. Team Members and Contributions

| Team Member | Register Number | Contribution |
|---|---|---|
| Muhammed Nilamuddeen (Team Leader) | 25190131 | Project setup, coordination, documentation, integration and testing |
| Saakshi | 25190143 | UI development, simulator display, UI-Core communication and testing |
| J. Subraya Shenoy | 25190117 |CoreProcess Implementation,CPU Implementation, FDE cycle, FIFO queue and Hamming Code implementation and testing |
| Manasi Maxin | 25190157 | Logger, message handling, timestamped logs and integration |

## 10. Project Folder Structure

The project is divided into different folders to keep the code organized and make it easier to work on individual modules.

```text
Nuvoton-microcontroller-simulator/
│
├── src/
│   ├── cpu/
│   ├── memory/
│   ├── instruction/
│   ├── stack/
│   ├── queue/
│   ├── process/
│   ├── ui/
│   ├── ipc/
│   └── log/
│
├── logs/
│   └── simulator.log
│
├── out/
│
└── README.md
```

## 11. How to Run the Project

### Requirements

- Java Development Kit (JDK).
- Visual Studio Code or any Java-supported IDE.
- Git (optional).

### Steps to Run

1. Clone or download the GitHub repository.
2. Open the project folder in Visual Studio Code.
3. Make sure Java is installed and configured.
4. Compile the Java source files.
5. Run the main UI class.
6. Enter the required assembly instructions.
7. Click LOAD to load the program.
8. Use STEP to execute instructions individually or RUN to execute the program.
9. Observe the changes in CPU registers, memory, stack, queue, and execution logs.

## 12. What We Learned

Working on this project helped us understand how different computer science concepts are used in a practical application.

During development, we gained experience in:

- Understanding microcontroller architecture.
- Working with CPU registers and instruction execution.
- Implementing memory and stack operations.
- Using FIFO queues and other data structures.
- Developing a graphical interface using Java Swing.
- Understanding threads and IPC.
- Working with socket communication.
- Implementing a logging system.
- Understanding Hamming Code and error detection.
- Using Git and GitHub for team collaboration.

## 13. Future Improvements

There are still several areas where the simulator can be improved in the future:

- Adding support for more microcontroller instructions.
- Improving GPIO and timer simulation.
- Adding a more complete interrupt mechanism.
- Extending process scheduling and performance analysis.
- Improving the memory and register visualization.
- Adding more error detection and correction features.

## 14. Conclusion

Through this project, we developed a Java-based educational simulator that demonstrates the basic working of the Nuvoton MS51FB9AE microcontroller.

We were able to bring together different concepts such as CPU instruction execution, memory management, stack operations, FIFO queues, inter-process communication, logging, and Hamming Code error detection.

Working on this project gave us a better understanding of how these concepts are connected and how they can be implemented in a real software application.

Overall, it was a valuable learning experience that helped us improve our programming, problem-solving, testing, and teamwork skills.
