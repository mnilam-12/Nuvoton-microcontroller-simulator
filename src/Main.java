import javax.swing.SwingUtilities;
import src.ui.SimulatorUI;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new SimulatorUI();
        });

    }
}