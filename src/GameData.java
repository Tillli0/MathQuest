import java.util.*;
import java.io.*;

/**
 * Gemeinsame Spielzustandsverwaltung und Highscore-System.
 */
public class GameData {

    // ─── Schwierigkeit ────────────────────────────────────────────────────
    public enum Difficulty {
        LEICHT("Leicht",   1, 5,  new java.awt.Color(60,180,80)),
        MITTEL("Mittel",   1, 10, new java.awt.Color(220,160,40)),
        SCHWER("Schwer",   7, 15, new java.awt.Color(200,50,50));

        public final String label;
        public final int min, max;
        public final java.awt.Color color;
        Difficulty(String label, int min, int max, java.awt.Color color) {
            this.label = label; this.min = min; this.max = max; this.color = color;
        }
        /** Liefert einen zufälligen Faktor im Bereich [min, max] */
        public int randomFactor() {
            return min + (int)(Math.random() * (max - min + 1));
        }
    }

    private static Difficulty currentDifficulty = Difficulty.MITTEL;

    public static void setDifficulty(Difficulty d) { currentDifficulty = d; }
    public static Difficulty getDifficulty()        { return currentDifficulty; }
    // Highscore-Eintrag
    public static class ScoreEntry implements Comparable<ScoreEntry> {
        public String name;
        public long avgMs;
        public int correct;

        public ScoreEntry(String name, long avgMs, int correct) {
            this.name = name;
            this.avgMs = avgMs;
            this.correct = correct;
        }

        @Override
        public int compareTo(ScoreEntry o) {
            // Weniger ms = besser; bei Gleichheit mehr Richtige
            if (this.avgMs != o.avgMs) return Long.compare(this.avgMs, o.avgMs);
            return Integer.compare(o.correct, this.correct);
        }
    }

    private static final String SCORE_FILE = "highscores.dat";
    private static List<ScoreEntry> highscores = new ArrayList<>();

    static {
        loadScores();
    }

    public static void addScore(String name, long avgMs, int correct) {
        highscores.add(new ScoreEntry(name, avgMs, correct));
        Collections.sort(highscores);
        if (highscores.size() > 10) {
            highscores = highscores.subList(0, 10);
        }
        saveScores();
    }

    public static List<ScoreEntry> getHighscores() {
        return Collections.unmodifiableList(highscores);
    }

    @SuppressWarnings("unchecked")
    private static void loadScores() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SCORE_FILE))) {
            highscores = (List<ScoreEntry>) ois.readObject();
        } catch (Exception e) {
            highscores = new ArrayList<>();
        }
    }

    private static void saveScores() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SCORE_FILE))) {
            oos.writeObject(highscores);
        } catch (Exception e) {
            // ignore
        }
    }

    // Einfache Serialisierung per Text als Fallback
    public static void saveScoresText() {
        try (PrintWriter pw = new PrintWriter(new FileWriter("highscores.txt"))) {
            for (ScoreEntry e : highscores) {
                pw.println(e.name + ";" + e.avgMs + ";" + e.correct);
            }
        } catch (Exception e) { /* ignore */ }
    }
}
