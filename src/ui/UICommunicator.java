
package src.ui;

public class UICommunicator {

    // Commands that the UI can send to the Core process.
    public static final String LOAD = "LOAD";
    public static final String RESET = "RESET";
    public static final String STEP = "STEP";
    public static final String RUN = "RUN";

    // Checks whether a command is valid.
    public boolean isValidCommand(String command) {
        return LOAD.equals(command)
                || RESET.equals(command)
                || STEP.equals(command)
                || RUN.equals(command);
    }

    // Prepares a command for the communication layer.
    public String prepareCommand(String command, String program) {
        if (!isValidCommand(command)) {
            throw new IllegalArgumentException(
                    "Unknown UI command: " + command);
        }

        if (LOAD.equals(command)) {
            if (program == null || program.trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "Program cannot be empty.");
            }

            return LOAD + "\n" + program;
        }

        return command;
    }

    // Processes a response received from the Core process.
    public String processResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return "No response received from Core process.";
        }

        return response.trim();
    }
}
