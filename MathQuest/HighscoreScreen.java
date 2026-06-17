import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.util.ArrayList;


/**
 * Highscore-Bildschirm mit animierter Tabelle.
 */
public class HighscoreScreen extends JFrame {

    private int tick = 0;
    private Timer animTimer;
    private java.util.List<GameData.ScoreEntry> scores;

    public HighscoreScreen() {
        scores = new ArrayList<>(GameData.getHighscores());
        setTitle("MathQuest – Highscores");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        ScorePanel panel = new ScorePanel();
        add(panel);
        animTimer = new Timer(16, e -> { tick++; panel.repaint(); });
        animTimer.start();
        setVisible(true);
    }

    class ScorePanel extends JPanel {

        private JButton backBtn;

        public ScorePanel() {
            setLayout(null);
            setOpaque(false);

            backBtn = new JButton("← Zurück zum Menü");
            backBtn.setBounds(280, 540, 240, 40);
            backBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
            backBtn.setBackground(new Color(60,30,120));
            backBtn.setForeground(Color.WHITE);
            backBtn.setFocusPainted(false);
            backBtn.setBorderPainted(false);
            backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            backBtn.addActionListener(e -> { animTimer.stop(); dispose(); new StartScreen(); });
            add(backBtn);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Hintergrund
            GradientPaint bg = new GradientPaint(0,0,new Color(10,5,30),0,600,new Color(25,10,60));
            g2.setPaint(bg); g2.fillRect(0,0,800,600);

            // Sterne
            Random rng = new Random(99);
            g2.setColor(new Color(255,255,255,120));
            for (int i=0;i<50;i++) {
                float sx = rng.nextFloat()*800;
                float sy = rng.nextFloat()*200;
                float alpha = (float)(0.4 + 0.6*Math.abs(Math.sin(tick*0.03+i)));
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha));
                g2.fillOval((int)sx,(int)sy,2,2);
            }
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1f));

            // Titel
            Font tf = new Font("SansSerif", Font.BOLD, 44);
            g2.setFont(tf);
            FontMetrics fm = g2.getFontMetrics();
            String title = "🏆 Highscores";
            // Glow
            g2.setColor(new Color(200,150,0,40));
            for (int d=12;d>0;d-=3)
                g2.drawString(title, (800-fm.stringWidth(title))/2-d/2, 70+d/2);
            g2.setColor(new Color(255,220,50));
            g2.drawString(title, (800-fm.stringWidth(title))/2, 70);

            // Pokal-Dekoration
            drawTrophy(g2, 90, 40);
            drawTrophy(g2, 685, 40);

            // Tabellen-Header
            int tableX = 80; int tableY = 105; int rowH = 44;
            g2.setColor(new Color(80,40,160));
            g2.fillRoundRect(tableX, tableY, 640, 36, 8, 8);
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.setColor(new Color(220,200,255));
            g2.drawString("#",   tableX+12,  tableY+24);
            g2.drawString("Name",tableX+50,  tableY+24);
            g2.drawString("Ø Zeit (ms)", tableX+300, tableY+24);
            g2.drawString("Richtig", tableX+480,tableY+24);
            g2.drawString("Wertung",tableX+570, tableY+24);

            // Einträge
            if (scores.isEmpty()) {
                g2.setFont(new Font("SansSerif", Font.ITALIC, 18));
                g2.setColor(new Color(140,100,200));
                g2.drawString("Noch keine Einträge – spiel dein erstes Spiel!", 160, 280);
            } else {
                for (int i = 0; i < scores.size() && i < 10; i++) {
                    GameData.ScoreEntry entry = scores.get(i);
                    int ry = tableY + 36 + i * rowH;

                    // Zeilen-Hintergrund
                    float rowAlpha = 1f - i * 0.05f;
                    if (i == 0) g2.setColor(new Color(120,90,10, 160));
                    else if (i == 1) g2.setColor(new Color(70,70,80, 140));
                    else if (i == 2) g2.setColor(new Color(80,50,20, 130));
                    else g2.setColor(new Color(30,15,60, 120));
                    g2.fillRoundRect(tableX, ry, 640, rowH-2, 6, 6);

                    // Rand-Highlight
                    if (i < 3) {
                        Color rc = i==0?new Color(220,180,0):i==1?new Color(160,160,170):new Color(160,100,40);
                        g2.setColor(rc);
                        g2.setStroke(new BasicStroke(1.5f));
                        g2.drawRoundRect(tableX, ry, 640, rowH-2, 6, 6);
                        g2.setStroke(new BasicStroke(1));
                    }

                    // Medaille
                    String medal = i==0?"🥇":i==1?"🥈":i==2?"🥉":"  ";
                    g2.setFont(new Font("SansSerif", Font.BOLD, 16));
                    g2.setColor(Color.WHITE);
                    g2.drawString(medal + " " + (i+1), tableX+8, ry+rowH/2+6);

                    // Name
                    g2.setColor(i<3 ? Color.WHITE : new Color(200,180,240));
                    g2.setFont(new Font("SansSerif", i<3?Font.BOLD:Font.PLAIN, 15));
                    g2.drawString(entry.name, tableX+50, ry+rowH/2+6);

                    // Zeit
                    Color tColor = entry.avgMs < 2000 ? new Color(80,220,100) :
                                   entry.avgMs < 4000 ? new Color(220,180,50) : new Color(220,100,80);
                    g2.setColor(tColor);
                    g2.setFont(new Font("SansSerif", Font.BOLD, 15));
                    g2.drawString(entry.avgMs + " ms", tableX+300, ry+rowH/2+6);

                    // Richtig
                    g2.setColor(new Color(100,200,120));
                    g2.drawString(entry.correct + " / 10", tableX+480, ry+rowH/2+6);

                    // Sterne
                    int st = entry.avgMs<2000&&entry.correct>=9 ? 3 :
                             entry.avgMs<4000&&entry.correct>=7 ? 2 : 1;
                    String stars = st==3?"★★★":st==2?"★★☆":"★☆☆";
                    g2.setColor(new Color(255,200,40));
                    g2.drawString(stars, tableX+570, ry+rowH/2+6);
                }
            }

            // Legende
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.setColor(new Color(100,80,150));
            g2.drawString("★★★ < 2000ms & 9+ richtig   ★★☆ < 4000ms & 7+ richtig   ★☆☆ sonst", 130, 535);
        }

        private void drawTrophy(Graphics2D g2, int cx, int cy) {
            float bob = (float) Math.sin(tick * 0.05) * 3;
            cy += (int) bob;

            g2.setColor(new Color(200,160,30));
            // Schale
            int[] tx = {cx-20, cx+20, cx+14, cx-14};
            int[] ty = {cy, cy, cy+25, cy+25};
            g2.fillPolygon(tx, ty, 4);
            // Henkel
            g2.setStroke(new BasicStroke(4));
            g2.drawArc(cx-26, cy+4, 14, 16, 90, 180);
            g2.drawArc(cx+12, cy+4, 14, 16, 270, 180);
            g2.setStroke(new BasicStroke(1));
            // Stiel
            g2.fillRoundRect(cx-4, cy+25, 8, 12, 2, 2);
            // Fuß
            g2.fillRoundRect(cx-12, cy+37, 24, 6, 3, 3);
            // Glanz
            g2.setColor(new Color(255,230,120,200));
            g2.fillOval(cx-10, cy+3, 6, 10);
        }
    }
}
