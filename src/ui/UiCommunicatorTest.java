
package src.ui;

public class UICommunicatorTest {

    public static void main(String[] args) {
        UICommunicator communicator = new UICommunicator();

        // Test the four commands used by the UI.
        String[] commands = {
            UICommunicator.LOAD,
            UICommunicator.RESET,
            UICommunicator.STEP,
            UICommunicator.RUN
        };

        for (String command : commands) {
            String result;

            if (UICommunicator.LOAD.equals(command)) {
                result = communicator.prepareCommand(
                    command, "MOV A, #05");
            } else {
                result = communicator.prepareCommand(
                    command, null);
            }

            System.out.println("Command: " + command);
            System.out.println("Prepared: " + result);
            System.out.println();
        }

        // Test how the UI handles a response.
        System.out.println(
            communicator.processResponse("STEP completed"));
    }
}
