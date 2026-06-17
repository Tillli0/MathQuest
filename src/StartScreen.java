import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Random;

/**
 * Startbildschirm mit animiertem Titel und Menü.
 */
public class StartScreen extends JFrame {

    private AnimatedPanel panel;

    public StartScreen() {
        setTitle("MathQuest – Der Kampf der Zahlen");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        panel = new AnimatedPanel();
        add(panel);

        setVisible(true);
    }

    // ─── Animiertes Haupt-Panel ────────────────────────────────────────────
    class AnimatedPanel extends JPanel implements ActionListener {

        private Timer timer;
        private float titleAlpha = 0f;
        private float btnAlpha = 0f;
        private int tick = 0;

        // Partikel (Sterne / Funken)
        private static final int NUM_STARS = 80;
        private float[] sx = new float[NUM_STARS];
        private float[] sy = new float[NUM_STARS];
        private float[] ss = new float[NUM_STARS]; // speed
        private float[] sa = new float[NUM_STARS]; // alpha
        private Color[] sc = new Color[NUM_STARS];

        // Buttons
        private Rectangle btnPlay   = new Rectangle(275, 360, 250, 55);
        private Rectangle btnScore  = new Rectangle(275, 430, 250, 55);
        private Rectangle btnQuit   = new Rectangle(275, 500, 250, 55);
        private Rectangle hoveredBtn = null;

        // Schwert-Wackeln
        private double swordAngle = 0;

        public AnimatedPanel() {
            setBackground(Color.BLACK);
            Random rng = new Random();
            for (int i = 0; i < NUM_STARS; i++) {
                sx[i] = rng.nextFloat() * 800;
                sy[i] = rng.nextFloat() * 600;
                ss[i] = 0.3f + rng.nextFloat() * 1.2f;
                sa[i] = rng.nextFloat();
                int c = rng.nextInt(3);
                sc[i] = c == 0 ? new Color(255,220,80) :
                        c == 1 ? new Color(100,180,255) :
                                 new Color(255,255,255);
            }

            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    Point p = e.getPoint();
                    if (btnPlay.contains(p))  startGame();
                    if (btnScore.contains(p)) showHighscores();
                    if (btnQuit.contains(p))  System.exit(0);
                }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    Point p = e.getPoint();
                    if (btnPlay.contains(p))       hoveredBtn = btnPlay;
                    else if (btnScore.contains(p)) hoveredBtn = btnScore;
                    else if (btnQuit.contains(p))  hoveredBtn = btnQuit;
                    else                            hoveredBtn = null;
                    setCursor(hoveredBtn != null ?
                        Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) :
                        Cursor.getDefaultCursor());
                }
            });

            timer = new Timer(16, this); // ~60 fps
            timer.start();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            tick++;
            // Fade-in
            if (titleAlpha < 1f) titleAlpha = Math.min(1f, titleAlpha + 0.012f);
            if (tick > 60 && btnAlpha < 1f) btnAlpha = Math.min(1f, btnAlpha + 0.018f);

            // Sterne
            Random rng = new Random();
            for (int i = 0; i < NUM_STARS; i++) {
                sy[i] -= ss[i];
                if (sy[i] < 0) {
                    sy[i] = 600;
                    sx[i] = rng.nextFloat() * 800;
                }
                sa[i] = (float)(0.4 + 0.6 * Math.abs(Math.sin(tick * 0.04 + i)));
            }

            swordAngle = Math.sin(tick * 0.07) * 0.18;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // ── Hintergrund-Gradient ──────────────────────────────────────
            GradientPaint bg = new GradientPaint(0, 0, new Color(10,5,30),
                                                  0, 600, new Color(30,10,60));
            g2.setPaint(bg);
            g2.fillRect(0, 0, 800, 600);

            // ── Sterne ────────────────────────────────────────────────────
            for (int i = 0; i < NUM_STARS; i++) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, sa[i]));
                g2.setColor(sc[i]);
                float sz = 1.5f + ss[i] * 0.8f;
                g2.fill(new Ellipse2D.Float(sx[i], sy[i], sz, sz));
            }
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

            // ── Held (Pixel-Art-Stil, einfach gezeichnet) ────────────────
            drawHero(g2, 130, 200);

            // ── Titel ─────────────────────────────────────────────────────
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, titleAlpha));

            // Glow hinter Titel
            for (int glow = 20; glow > 0; glow -= 4) {
                g2.setColor(new Color(120, 60, 255, 10));
                Font gf = new Font("SansSerif", Font.BOLD, 54 + glow);
                g2.setFont(gf);
                FontMetrics fm = g2.getFontMetrics();
                int tx = (800 - fm.stringWidth("MathQuest")) / 2;
                g2.drawString("MathQuest", tx - glow/2, 130 + glow/2);
            }

            g2.setColor(new Color(255, 220, 60));
            drawOutlinedText(g2, "MathQuest", new Font("SansSerif", Font.BOLD, 54),
                             new Color(255,220,60), new Color(180,80,0), 400, 130);

            g2.setColor(new Color(180,140,255));
            drawOutlinedText(g2, "Der Kampf der Zahlen",
                             new Font("SansSerif", Font.ITALIC, 22),
                             new Color(200,170,255), new Color(80,30,120), 400, 165);

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

            // ── Buttons ───────────────────────────────────────────────────
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, btnAlpha));
            drawButton(g2, btnPlay,  "⚔  Spiel starten",  btnPlay  == hoveredBtn);
            drawButton(g2, btnScore, "� Highscores",      btnScore == hoveredBtn);
            drawButton(g2, btnQuit,  "✖  Beenden",         btnQuit  == hoveredBtn);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

            // ── Untertitel ────────────────────────────────────────────────
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.setColor(new Color(120,100,160));
            g2.drawString("Löse 10 Aufgaben aus dem 1×1 so schnell wie möglich!", 215, 578);
        }

        private void drawButton(Graphics2D g2, Rectangle r, String label, boolean hovered) {
            // Schatten
            g2.setColor(new Color(0,0,0,80));
            g2.fillRoundRect(r.x+4, r.y+4, r.width, r.height, 20, 20);

            // Körper
            GradientPaint gp;
            if (hovered) {
                gp = new GradientPaint(r.x, r.y, new Color(120,60,220),
                                        r.x, r.y+r.height, new Color(60,20,140));
            } else {
                gp = new GradientPaint(r.x, r.y, new Color(70,30,150),
                                        r.x, r.y+r.height, new Color(30,10,80));
            }
            g2.setPaint(gp);
            g2.fillRoundRect(r.x, r.y, r.width, r.height, 20, 20);

            // Rand
            g2.setColor(hovered ? new Color(220,180,255) : new Color(130,80,200));
            g2.setStroke(new BasicStroke(hovered ? 2.5f : 1.5f));
            g2.drawRoundRect(r.x, r.y, r.width, r.height, 20, 20);
            g2.setStroke(new BasicStroke(1f));

            // Text
            Font f = new Font("SansSerif", Font.BOLD, 17);
            g2.setFont(f);
            FontMetrics fm = g2.getFontMetrics();
            int tx = r.x + (r.width - fm.stringWidth(label)) / 2;
            int ty = r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2;
            g2.setColor(hovered ? Color.WHITE : new Color(220, 200, 255));
            g2.drawString(label, tx, ty);
        }

        private void drawOutlinedText(Graphics2D g2, String text, Font font,
                                       Color fill, Color outline, int cx, int y) {
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();
            int x = cx - fm.stringWidth(text) / 2;
            g2.setColor(outline);
            for (int dx = -2; dx <= 2; dx++)
                for (int dy = -2; dy <= 2; dy++)
                    if (dx != 0 || dy != 0)
                        g2.drawString(text, x + dx, y + dy);
            g2.setColor(fill);
            g2.drawString(text, x, y);
        }

        /** Einfacher pixeliger Held links im Bild */
        private void drawHero(Graphics2D g2, int cx, int cy) {
            // Schatten
            g2.setColor(new Color(0,0,0,60));
            g2.fillOval(cx-28, cy+95, 56, 16);

            // Körper
            g2.setColor(new Color(60,90,200));
            g2.fillRoundRect(cx-18, cy+30, 36, 50, 8, 8);

            // Umhang
            g2.setColor(new Color(180,20,20));
            int[] capX = {cx-22, cx+22, cx+28, cx-28};
            int[] capY = {cy+35, cy+35, cy+90, cy+90};
            g2.fillPolygon(capX, capY, 4);

            // Kopf
            g2.setColor(new Color(255,210,170));
            g2.fillOval(cx-16, cy, 32, 30);

            // Haare
            g2.setColor(new Color(120,60,10));
            g2.fillArc(cx-16, cy-4, 32, 20, 0, 180);

            // Augen
            g2.setColor(Color.WHITE);
            g2.fillOval(cx-10, cy+8, 8, 7);
            g2.fillOval(cx+2,  cy+8, 8, 7);
            g2.setColor(new Color(30,60,180));
            g2.fillOval(cx-8,  cy+10, 5, 5);
            g2.fillOval(cx+4,  cy+10, 5, 5);

            // Beine
            g2.setColor(new Color(40,40,80));
            g2.fillRoundRect(cx-14, cy+78, 12, 20, 4, 4);
            g2.fillRoundRect(cx+2,  cy+78, 12, 20, 4, 4);

            // Schwert (animiert wackeln)
            Graphics2D sg = (Graphics2D) g2.create();
            sg.translate(cx+18, cy+50);
            sg.rotate(swordAngle);
            // Klinge
            sg.setColor(new Color(200,220,255));
            sg.fillRoundRect(0, -4, 50, 8, 4, 4);
            sg.setColor(new Color(150,180,220));
            sg.drawRoundRect(0, -4, 50, 8, 4, 4);
            // Glanz
            sg.setColor(new Color(255,255,255,180));
            sg.fillRoundRect(5, -2, 30, 3, 2, 2);
            // Griff
            sg.setColor(new Color(120,70,20));
            sg.fillRoundRect(-12, -5, 12, 10, 3, 3);
            // Parierstange
            sg.setColor(new Color(200,160,50));
            sg.fillRoundRect(-4, -9, 8, 18, 2, 2);
            sg.dispose();

            // Schild
            g2.setColor(new Color(200,150,40));
            int[] shX = {cx-26, cx-40, cx-40, cx-26};
            int[] shY = {cy+28, cy+32, cy+62, cy+66};
            g2.fillPolygon(shX, shY, 4);
            g2.setColor(new Color(160,100,10));
            g2.drawPolygon(shX, shY, 4);
            // Schildmuster
            g2.setColor(new Color(220,60,60));
            g2.fillOval(cx-38, cy+43, 10, 10);
        }

        private void startGame() {
            dispose();
            new GameScreen();
        }

        private void showHighscores() {
            dispose();
            new HighscoreScreen();
        }
    }
}
