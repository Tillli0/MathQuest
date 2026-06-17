import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Random;
import java.util.ArrayList;

/**
 * Ergebnis-Bildschirm nach 10 Aufgaben.
 * Zeigt Auswertung, Zeitdiagramm und Namenseingabe fÃ¼r Highscore.
 */
public class ResultScreen extends JFrame {

    private long avgMs;
    private int correctCount;
    private long[] times;
    private String playerName = "";
    private boolean submitted = false;
    private int tick = 0;
    private Timer animTimer;

    // Sterne fÃ¼r Bewertung
    private int stars;
    // Konfetti
    private java.util.List<ConfettiPiece> confetti = new ArrayList<>();

    public ResultScreen(long avgMs, int correctCount, long[] times) {
        this.avgMs = avgMs;
        this.correctCount = correctCount;
        this.times = times;

        // Sterne berechnen
        if (avgMs < 2000 && correctCount >= 9) stars = 3;
        else if (avgMs < 4000 && correctCount >= 7) stars = 2;
        else stars = 1;

        if (stars == 3) spawnConfetti();

        setTitle("MathQuest â€“ Ergebnis");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        ResultPanel panel = new ResultPanel();
        add(panel);

        animTimer = new Timer(16, e -> { tick++; updateConfetti(); panel.repaint(); });
        animTimer.start();

        setVisible(true);
    }

    private void spawnConfetti() {
        Random rng = new Random();
        Color[] cols = {new Color(255,80,80), new Color(80,220,80), new Color(80,80,255),
                         new Color(255,220,0), new Color(255,100,200), new Color(0,200,220)};
        for (int i = 0; i < 120; i++) {
            confetti.add(new ConfettiPiece(rng.nextFloat()*800, -10 - rng.nextFloat()*200,
                    (float)(rng.nextGaussian()*1.5), 1f + rng.nextFloat()*3f,
                    cols[rng.nextInt(cols.length)],
                    rng.nextFloat() * (float) Math.PI * 2,
                    (float)(rng.nextGaussian()*0.1)));
        }
    }

    private void updateConfetti() {
        for (ConfettiPiece c : confetti) c.update();
    }

    static class ConfettiPiece {
        float x, y, vx, vy, angle, spin;
        Color color;
        ConfettiPiece(float x, float y, float vx, float vy, Color c, float angle, float spin) {
            this.x=x; this.y=y; this.vx=vx; this.vy=vy; this.color=c; this.angle=angle; this.spin=spin;
        }
        void update() { x+=vx; y+=vy; vy+=0.05f; angle+=spin; if(y>620) y=-10; }
    }

    class ResultPanel extends JPanel implements ActionListener {
        private JTextField nameField;
        private JButton submitBtn, playAgainBtn, menuBtn, scoreBtn;

        public ResultPanel() {
            setLayout(null);
            setBackground(new Color(10,5,30));

            // Namenseingabe
            nameField = new JTextField("Spieler");
            nameField.setBounds(240, 455, 180, 38);
            nameField.setFont(new Font("SansSerif", Font.BOLD, 16));
            nameField.setBackground(new Color(20,10,50));
            nameField.setForeground(Color.WHITE);
            nameField.setCaretColor(Color.WHITE);
            nameField.setBorder(BorderFactory.createLineBorder(new Color(140,80,220), 2));
            nameField.setHorizontalAlignment(JTextField.CENTER);
            add(nameField);

            submitBtn = makeButton("ðŸ?† Eintragen", 430, 455, 140, 38, new Color(80,30,160));
            submitBtn.addActionListener(e -> {
                if (!submitted) {
                    String name = nameField.getText().trim();
                    if (name.isEmpty()) name = "Unbekannt";
                    GameData.addScore(name, avgMs, correctCount);
                    submitted = true;
                    submitBtn.setText("âœ“ Eingetragen!");
                    submitBtn.setEnabled(false);
                }
            });
            add(submitBtn);

            playAgainBtn = makeButton("âš” Nochmal", 140, 520, 160, 40, new Color(60,120,30));
            playAgainBtn.addActionListener(e -> { animTimer.stop(); dispose(); new GameScreen(); });
            add(playAgainBtn);

            scoreBtn = makeButton("ðŸ?† Highscores", 320, 520, 160, 40, new Color(100,60,20));
            scoreBtn.addActionListener(e -> { animTimer.stop(); dispose(); new HighscoreScreen(); });
            add(scoreBtn);

            menuBtn = makeButton("ðŸ?  MenÃ¼", 500, 520, 160, 40, new Color(40,40,100));
            menuBtn.addActionListener(e -> { animTimer.stop(); dispose(); new StartScreen(); });
            add(menuBtn);
        }

        private JButton makeButton(String label, int x, int y, int w, int h, Color bg) {
            JButton btn = new JButton(label);
            btn.setBounds(x, y, w, h);
            btn.setFont(new Font("SansSerif", Font.BOLD, 14));
            btn.setBackground(bg);
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return btn;
        }

        @Override
        public void actionPerformed(ActionEvent e) { repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Hintergrund
            GradientPaint bg = new GradientPaint(0,0,new Color(10,5,30),0,600,new Color(20,10,50));
            g2.setPaint(bg); g2.fillRect(0,0,800,600);

            // Konfetti
            for (ConfettiPiece c : confetti) {
                Graphics2D cg = (Graphics2D) g2.create();
                cg.translate(c.x, c.y);
                cg.rotate(c.angle);
                cg.setColor(c.color);
                cg.fillRect(-5, -3, 10, 6);
                cg.dispose();
            }

            // Titel
            drawOutlined(g2, "Kampf beendet!", new Font("SansSerif", Font.BOLD, 42),
                          new Color(255,220,60), new Color(140,80,0), 400, 60);

            // Sterne
            drawStars(g2);

            // Statistik-Box
            g2.setColor(new Color(0,0,0,140));
            g2.fillRoundRect(60, 110, 330, 160, 16, 16);
            g2.setColor(new Color(140,80,220));
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(60, 110, 330, 160, 16, 16);
            g2.setStroke(new BasicStroke(1));

            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2.setColor(new Color(200,170,255));
            g2.drawString("ðŸ“Š Auswertung", 75, 135);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 15));
            g2.setColor(Color.WHITE);
            g2.drawString("Richtige Antworten:", 75, 162);
            g2.setColor(new Color(80,220,100));
            g2.setFont(new Font("SansSerif", Font.BOLD, 15));
            g2.drawString(correctCount + " / 10", 280, 162);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 15));
            g2.drawString("Ã˜ Bearbeitungszeit:", 75, 186);
            Color tColor = avgMs < 2000 ? new Color(80,220,100) :
                           avgMs < 4000 ? new Color(220,180,50) : new Color(220,80,80);
            g2.setColor(tColor);
            g2.setFont(new Font("SansSerif", Font.BOLD, 15));
            g2.drawString(avgMs + " ms", 280, 186);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 15));
            g2.drawString("Schnellste Antwort:", 75, 210);
            long fastest = Long.MAX_VALUE;
            for (long t : times) if (t < fastest) fastest = t;
            g2.setColor(new Color(80,220,100));
            g2.setFont(new Font("SansSerif", Font.BOLD, 15));
            g2.drawString(fastest + " ms", 280, 210);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 15));
            g2.drawString("Langsamste Antwort:", 75, 234);
            long slowest = 0;
            for (long t : times) if (t > slowest) slowest = t;
            g2.setColor(new Color(220,120,80));
            g2.setFont(new Font("SansSerif", Font.BOLD, 15));
            g2.drawString(slowest + " ms", 280, 234);

            // Zeitdiagramm
            drawTimeChart(g2);

            // Namenseingabe-Label
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.setColor(new Color(200,170,255));
            g2.drawString("Name fÃ¼r Highscore:", 240, 450);
        }

        private void drawStars(Graphics2D g2) {
            int[] starX = {320, 400, 480};
            for (int i = 0; i < 3; i++) {
                boolean lit = i < stars;
                drawStar(g2, starX[i], 95, 22,
                         lit ? new Color(255,220,30) : new Color(60,50,80),
                         lit ? new Color(200,140,0) : new Color(40,35,60));
            }
        }

        private void drawStar(Graphics2D g2, int cx, int cy, int r, Color fill, Color outline) {
            int points = 5;
            int[] xp = new int[points*2];
            int[] yp = new int[points*2];
            float pulse = (float) Math.sin(tick * 0.08) * 2;
            for (int i = 0; i < points*2; i++) {
                double angle = Math.PI * i / points - Math.PI/2;
                int rad = (i % 2 == 0) ? r + (int)pulse : r/2;
                xp[i] = cx + (int)(Math.cos(angle) * rad);
                yp[i] = cy + (int)(Math.sin(angle) * rad);
            }
            g2.setColor(outline);
            g2.fillPolygon(xp, yp, points*2);
            g2.setColor(fill);
            int[] xi = new int[points*2]; int[] yi = new int[points*2];
            for (int i = 0; i < points*2; i++) {
                double angle = Math.PI * i / points - Math.PI/2;
                int rad = (i%2==0) ? r-2+(int)pulse : r/2-2;
                xi[i] = cx + (int)(Math.cos(angle)*rad);
                yi[i] = cy + (int)(Math.sin(angle)*rad);
            }
            g2.fillPolygon(xi, yi, points*2);
        }

        private void drawTimeChart(Graphics2D g2) {
            int ox = 430, oy = 280, w = 330, h = 130;

            // Box
            g2.setColor(new Color(0,0,0,140));
            g2.fillRoundRect(ox, 110, w, h+60, 16, 16);
            g2.setColor(new Color(140,80,220));
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(ox, 110, w, h+60, 16, 16);
            g2.setStroke(new BasicStroke(1));

            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.setColor(new Color(200,170,255));
            g2.drawString("â?± Zeitverlauf (ms)", ox+10, 130);

            // Achsen
            g2.setColor(new Color(100,80,150));
            g2.drawLine(ox+20, oy, ox+20, oy-h);
            g2.drawLine(ox+20, oy, ox+w-10, oy);

            // Balken
            long maxT = 0;
            for (long t : times) if (t > maxT) maxT = t;
            if (maxT == 0) maxT = 1;

            int barW = (w - 40) / times.length - 2;
            for (int i = 0; i < times.length; i++) {
                int bh = (int)((float) times[i] / maxT * (h-10));
                int bx = ox + 22 + i * (barW + 2);
                int by = oy - bh;

                // Farbe: grÃ¼n wenn korrekt, getrennter check nicht mÃ¶glich ohne extra array
                Color bc = times[i] < avgMs ? new Color(80,200,100) : new Color(200,80,80);
                GradientPaint gp = new GradientPaint(bx, by, bc.brighter(), bx, oy, bc.darker());
                g2.setPaint(gp);
                g2.fillRoundRect(bx, by, barW, bh, 3, 3);

                // Nr
                g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
                g2.setColor(new Color(160,140,200));
                g2.drawString(""+(i+1), bx+barW/2-3, oy+12);
            }

            // Durchschnittslinie
            int avgY = oy - (int)((float) avgMs / maxT * (h-10));
            g2.setColor(new Color(255,220,50,200));
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
                                           0, new float[]{4,4}, 0));
            g2.drawLine(ox+20, avgY, ox+w-10, avgY);
            g2.setStroke(new BasicStroke(1));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.setColor(new Color(255,220,50));
            g2.drawString("Ã˜", ox+w-22, avgY-2);
        }

        private void drawOutlined(Graphics2D g2, String text, Font font,
                                   Color fill, Color outline, int cx, int y) {
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();
            int x = cx - fm.stringWidth(text) / 2;
            g2.setColor(outline);
            for (int dx=-2;dx<=2;dx++) for(int dy=-2;dy<=2;dy++) if(dx!=0||dy!=0)
                g2.drawString(text, x+dx, y+dy);
            g2.setColor(fill);
            g2.drawString(text, x, y);
        }
    }
}
