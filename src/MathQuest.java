import javax.swing.*;

/**
 * MathQuest - Das Kopfrechenspiel
 * Starte das Spiel mit der main()-Methode.
 * Kompatibel mit BlueJ.
 */
public class MathQuest {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new StartScreen();
        });
    }
}
