# Nuvoton-microcontroller-simulator
Nuvoton MS51FB9AE microcontroller simulator

## Project title: Educational Microcontroller Simulator with process

## Problem Objective:
To develop a simplified educational microcontroller simulator that demonstrates instruction execution,memory and peripheral operations and process management.The project integrates microprocessor architecture,data structure and operating systems concepts into single working systems.The simulator helps user to visualixe how programs are executes and how CPU manages multiple program.

## Problem Statement: 
Design and implement a software based simulator for the Nuvoton MS51FB9AE 8-bit microcontroller.The simulator will model basic processor components,instruction execution,memory,stack,GPIO,timer and interupts.It will also support multiple processes using PCB,ready queue,context texting and FCFS

## Project scope:
Simulate the basic components of the Nuvoton MS51FB9AE microcontroller.
Implement selected instructions, memory, stack, GPIO, timer, and interrupts.
Manage multiple programs as processes using PCB and ready queue.
Implement FCFS, Round Robin, and Priority scheduling with context switching.
Provide program loading, run, reset, and single-step execution.
Analyze waiting time, turnaround time, response time, context switches, and CPU utilization.


## Microcontroller being simulated:
Nuvoton MS51FB9AE a 8 bit 8051-based microcontroller,this simulator will model the essential architectural features required for the project.

## Team members: 
Team leader---Muhammed Nilamuddeen-25190131
              J Subraya Shenoy-25190117
              Saakshi-25190143
              Manasi maxin-25190157

## Team responsibilities:
RESPONSIBILITIES
Muhammed Nilamuddeen                  
Primary:OS scheduling and context switching
Secondary:Integration and github
                                       
Saakshi                                
Primary:Memory and stack
Secondary: Testing
                                       
J Subraya shenoy                       
Primary: CPU & Instruction execution
Secondary:UI and integration,Meetings
                                       
Mansi maxin                            
Primary:Data structures  and process management
Secondary:CPU support 

## Selected programming language:

## Initial system architecture:
             MS51FB9AE Microcontroller Simulator
                                   │
             ┌─────────────────────┼─────────────────────┐
             │                     │                     │
          CPU CORE               MEMORY             PERIPHERALS
             │                     │                     │
       ┌─────┼─────┐         ┌─────┼─────┐          ┌────┴────┐    
       flags PC   SP      Program  RAM  Stack     GPIO     Timer
                          Memory
             │                     │                     │
             └─────────────────────┼─────────────────────┘
                                   
                         
                          PROCESS MANAGER  
                          Coordinates CPU, 
                          Memory & I/O     
                                 │
                                 │
                         
                             SCHEDULER                
                            Controls the     
                          simulation steps 
                                  │
                    
                CPU STEP     TIMER CHECK   INTERRUPT CHECK
                    │             │             │
                    
                            USER INTERFACE                  
                             Load Program     
                              Run / Step       
                                Reset            
                              View State       
                         
                                  │
                    ┌─────────────┼─────────────┐
                Registers       Memory         GPIO
                   View          View           View

## Initial development plan:

We will develop the simulator step by step over 8 weeks.

### Week 1 – Understanding the Microcontroller

* Learn the basic architecture of MS51FB9AE.
* Understand CPU, registers, PC, SP and flags.
* Study memory, stack, GPIO, timer and interrupts.
* Prepare the initial system architecture.

### Week 2 – CPU and Instructions

* Create the basic CPU structure.
* Add registers and flags.
* Understand instruction fetch and execution.
* Implement the required basic instructions.
* Test instruction execution.

### Week 3 – Memory and Stack

* Implement program memory and RAM.
* Implement the stack and Stack Pointer.
* Add read and write operations.
* Test memory and stack operations.

### Week 4 – GPIO, Timer and Interrupts

* Implement basic GPIO operations.
* Add timer functionality.
* Add interrupt handling.
* Test the peripherals with the CPU.

### Week 5 – Process Management

* Create processes from loaded programs.
* Create the PCB.
* Create the ready queue.
* Add process states such as Ready, Running and Completed.

### Week 6 – Scheduling and Context Switching

* Implement FCFS scheduling.
* Implement Round Robin and Priority scheduling.
* Add context switching.
* Calculate waiting time, turnaround time and response time.

### Week 7 – Integration and User Interface

* Connect CPU, memory, peripherals and process management.
* Add program loading.
* Add Run, Step and Reset options.
* Display registers, memory, GPIO and process information.

### Week 8 – Testing and Finalization

* Test all the components together.
* Find and fix errors.
* Check scheduling results and CPU performance.
* Improve the user interface.
* Complete the documentation and final project.


 
