import GUI.MainWindow;
import javax.swing.*;

/**
 * Entry point for the LAN File Transfer GUI application.
 *
 * Build:
 *   find src -name "*.java" | xargs javac --source 17 --release 17 -d out -sourcepath src
 *
 * Run:
 *   java -cp out Main
 */
public class Main {
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}

        // Force dark title-bar on Windows 11 where possible
        System.setProperty("sun.java2d.uiScale", "1");

        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }
}
