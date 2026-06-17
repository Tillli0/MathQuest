import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Random;
import java.util.ArrayList;
import java.util.Iterator;
/**
 * Hauptspielbildschirm: Held kämpft gegen Monster.
 * Jede richtige Antwort = Schwerthieb, falsche = Gegner schlägt zurück.
 */
public class GameScreen extends JFrame {

    static final int TOTAL_QUESTIONS = 10;

    // Spielzustand
    private int questionIndex = 0;
    private int factor1, factor2, correctAnswer;
    private long questionStartMs;
    private long[] times = new long[TOTAL_QUESTIONS];
    private int correctCount = 0;

    // Animation
    private boolean heroAttacking   = false;
    private boolean enemyAttacking  = false;
    private boolean showFlash       = false;
    private float   flashAlpha      = 0f;
    private int     heroShake       = 0;
    private int     enemyShake      = 0;
    private float   heroX           = 160f;
    private float   enemyX          = 580f;
    private int     heroHP          = 10;
    private int     enemyHP         = 10;
    private float   heroAttackX     = 160f;

    // Partikel
    private java.util.List<Particle> particles = new ArrayList<>();

    // UI-Elemente
    private GamePanel panel;
    private JTextField answerField;
    private JButton submitButton;
    private JLabel questionLabel;
    private JLabel statusLabel;

    // Feedback-Text
    private String feedbackText = "";
    private Color feedbackColor = Color.WHITE;
    private int feedbackAlpha = 0;

    // Gegner
    private int currentEnemy = 0; // Welches Monster
    private static final String[] ENEMY_NAMES = {
        "Goblin",  "Skelett", "Troll",   "Werwolf",
        "Drache",  "Lich",    "Oger",    "Vampire",
        "Golem",   "Hydra"
    };
    private static final Color[] ENEMY_COLORS = {
        new Color(60,160,60),   new Color(200,200,200), new Color(100,80,50),
        new Color(140,80,140),  new Color(180,50,20),   new Color(60,20,120),
        new Color(100,70,30),   new Color(180,20,80),   new Color(100,100,120),
        new Color(20,120,100)
    };

    private Timer animTimer;
    private int tick = 0;

    public GameScreen() {
        setTitle("MathQuest – Kampf!");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null);

        panel = new GamePanel();
        panel.setBounds(0, 0, 800, 600);
        add(panel);

        // Eingabefeld
        answerField = new JTextField();
        answerField.setBounds(290, 475, 140, 44);
        answerField.setFont(new Font("SansSerif", Font.BOLD, 26));
        answerField.setHorizontalAlignment(JTextField.CENTER);
        answerField.setBackground(new Color(20,10,50));
        answerField.setForeground(Color.WHITE);
        answerField.setCaretColor(Color.WHITE);
        answerField.setBorder(BorderFactory.createLineBorder(new Color(140,80,220), 2));
        panel.add(answerField);

        // Submit-Button
        submitButton = new JButton("⚔ Angriff!");
        submitButton.setBounds(440, 475, 120, 44);
        submitButton.setFont(new Font("SansSerif", Font.BOLD, 15));
        submitButton.setBackground(new Color(100,30,180));
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);
        submitButton.setBorderPainted(false);
        submitButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        submitButton.addActionListener(e -> checkAnswer());
        panel.add(submitButton);

        answerField.addActionListener(e -> checkAnswer());

        // Animation-Timer
        animTimer = new Timer(16, e -> {
            tick++;
            updateAnimations();
            panel.repaint();
        });
        animTimer.start();

        generateQuestion();
        setVisible(true);
        answerField.requestFocus();
    }

    private void generateQuestion() {
        Random rng = new Random();
        factor1 = 1 + rng.nextInt(10);
        factor2 = 1 + rng.nextInt(10);
        correctAnswer = factor1 * factor2;
        currentEnemy = questionIndex % ENEMY_NAMES.length;
        enemyHP = 10;
        questionStartMs = System.currentTimeMillis();
        feedbackText = "";
        answerField.setText("");
        answerField.requestFocus();
    }

    private void checkAnswer() {
        String input = answerField.getText().trim();
        if (input.isEmpty()) return;

        long elapsed = System.currentTimeMillis() - questionStartMs;
        times[questionIndex] = elapsed;

        int answer;
        try {
            answer = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            feedbackText = "Nur Zahlen eingeben!";
            feedbackColor = new Color(255,80,80);
            feedbackAlpha = 255;
            return;
        }

        if (answer == correctAnswer) {
            correctCount++;
            feedbackText = "✓ Richtig! (" + elapsed + " ms)";
            feedbackColor = new Color(80,255,120);
            feedbackAlpha = 255;
            heroHP = Math.min(10, heroHP);
            enemyHP = 0;
            triggerHeroAttack();
            spawnHitParticles((int) enemyX, 250, ENEMY_COLORS[currentEnemy]);
        } else {
            feedbackText = "✗ Falsch! Richtig: " + factor1 + " × " + factor2 + " = " + correctAnswer;
            feedbackColor = new Color(255,80,80);
            feedbackAlpha = 255;
            heroHP = Math.max(0, heroHP - 2);
            triggerEnemyAttack();
            spawnHitParticles((int) heroX, 270, new Color(255,100,100));
        }

        questionIndex++;
        if (questionIndex >= TOTAL_QUESTIONS) {
            // Kleine Verzögerung, dann Ergebnis
            answerField.setEnabled(false);
            submitButton.setEnabled(false);
            Timer endTimer = new Timer(1800, ev -> {
                animTimer.stop();
                showResult();
            });
            endTimer.setRepeats(false);
            endTimer.start();
        } else {
            Timer next = new Timer(900, ev -> generateQuestion());
            next.setRepeats(false);
            next.start();
        }
    }

    private void triggerHeroAttack() {
        heroAttacking = true;
        Timer t = new Timer(350, e -> { heroAttacking = false; heroAttackX = 160f; });
        t.setRepeats(false);
        t.start();
        showFlash = true;
        flashAlpha = 1f;
        enemyShake = 12;
    }

    private void triggerEnemyAttack() {
        enemyAttacking = true;
        Timer t = new Timer(350, e -> enemyAttacking = false);
        t.setRepeats(false);
        t.start();
        heroShake = 12;
        flashAlpha = 0.6f;
        showFlash = true;
    }

    private void spawnHitParticles(int x, int y, Color col) {
        Random rng = new Random();
        for (int i = 0; i < 18; i++) {
            particles.add(new Particle(x, y,
                (float)(rng.nextGaussian() * 4),
                (float)(rng.nextGaussian() * 4 - 2),
                col, 50 + rng.nextInt(30)));
        }
    }

    private void updateAnimations() {
        // Hero läuft vor zum Angriff
        if (heroAttacking) {
            if (heroAttackX < enemyX - 100)
                heroAttackX += 14f;
        } else {
            if (heroAttackX > 160f)
                heroAttackX -= 18f;
            else heroAttackX = 160f;
        }
        // Flash
        if (flashAlpha > 0f) flashAlpha = Math.max(0f, flashAlpha - 0.07f);
        else showFlash = false;
        // Schütteln
        if (heroShake > 0)  heroShake--;
        if (enemyShake > 0) enemyShake--;

        // Partikel
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.update();
            if (p.isDead()) it.remove();
        }
        // Feedback ausblenden
        if (feedbackAlpha > 0) feedbackAlpha = Math.max(0, feedbackAlpha - 3);
    }

    private void showResult() {
        long total = 0;
        for (long t : times) total += t;
        long avg = total / TOTAL_QUESTIONS;
        dispose();
        new ResultScreen(avg, correctCount, times);
    }

    // ─── Innere Klasse: Partikel ───────────────────────────────────────────
    static class Particle {
        float x, y, vx, vy;
        Color color;
        int life, maxLife;

        Particle(float x, float y, float vx, float vy, Color c, int life) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy;
            this.color = c; this.life = this.maxLife = life;
        }
        void update() { x += vx; y += vy; vy += 0.18f; life--; }
        boolean isDead() { return life <= 0; }
        float alpha() { return (float) life / maxLife; }
    }

    // ─── Innere Klasse: Haupt-Panel ────────────────────────────────────────
    class GamePanel extends JPanel {
        public GamePanel() { setLayout(null); setOpaque(false); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // ── Hintergrund ───────────────────────────────────────────────
            drawBackground(g2);

            // ── Flash-Effekt ──────────────────────────────────────────────
            if (showFlash && flashAlpha > 0f) {
                Color fc = heroAttacking ? new Color(255,220,0,(int)(flashAlpha*120)) :
                                           new Color(255,50,50,(int)(flashAlpha*100));
                g2.setColor(fc);
                g2.fillRect(0, 0, 800, 600);
            }

            // ── HP-Balken ─────────────────────────────────────────────────
            drawHPBar(g2, 40, 20, heroHP, 10, "Held", new Color(50,200,80));
            drawHPBar(g2, 500, 20, enemyHP, 10, ENEMY_NAMES[currentEnemy], new Color(220,60,60));

            // ── Fortschrittsleiste ────────────────────────────────────────
            drawProgressBar(g2);

            // ── Arena-Boden ───────────────────────────────────────────────
            drawArenaFloor(g2);

            // ── Gegner ────────────────────────────────────────────────────
            int ex = (int) enemyX + (enemyShake > 0 ? (tick % 2 == 0 ? -4 : 4) : 0);
            drawEnemy(g2, ex, 200, currentEnemy);

            // ── Held ──────────────────────────────────────────────────────
            int hx = (int) heroAttackX + (heroShake > 0 ? (tick % 2 == 0 ? -4 : 4) : 0);
            drawHero(g2, hx, 200);

            // ── Partikel ──────────────────────────────────────────────────
            for (Particle p : particles) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, p.alpha()));
                g2.setColor(p.color);
                g2.fillOval((int)p.x - 4, (int)p.y - 4, 8, 8);
                g2.setColor(p.color.brighter());
                g2.fillOval((int)p.x - 2, (int)p.y - 2, 4, 4);
            }
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

            // ── Aufgabe ───────────────────────────────────────────────────
            drawQuestion(g2);

            // ── Feedback-Text ─────────────────────────────────────────────
            if (feedbackAlpha > 0) {
                Color fc = new Color(feedbackColor.getRed(), feedbackColor.getGreen(),
                                     feedbackColor.getBlue(), feedbackAlpha);
                g2.setFont(new Font("SansSerif", Font.BOLD, 20));
                g2.setColor(fc);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(feedbackText, (800 - fm.stringWidth(feedbackText)) / 2, 460);
            }

            // ── Frage-Nr. ────────────────────────────────────────────────
            g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
            g2.setColor(new Color(160,130,200));
            g2.drawString("Aufgabe " + (questionIndex + 1) + " / " + TOTAL_QUESTIONS, 10, 595);
        }

        private void drawBackground(Graphics2D g2) {
            // Himmel
            GradientPaint sky = new GradientPaint(0,0, new Color(15,5,40),
                                                   0,350, new Color(40,15,80));
            g2.setPaint(sky);
            g2.fillRect(0, 0, 800, 600);

            // Burg im Hintergrund (einfach)
            g2.setColor(new Color(30,15,55));
            g2.fillRect(320, 120, 160, 180);
            g2.fillRect(300, 100, 50, 60);
            g2.fillRect(450, 100, 50, 60);
            g2.fillRect(370, 85, 60, 50);
            // Zinnen
            for (int i = 0; i < 4; i++) {
                g2.fillRect(300 + i*18, 90, 10, 12);
                g2.fillRect(450 + i*18, 90, 10, 12);
            }
            // Burgtor
            g2.setColor(new Color(10,5,20));
            g2.fillRoundRect(380, 220, 40, 80, 20, 20);
            // Fenster
            g2.setColor(new Color(255,220,80,120));
            g2.fillOval(345, 145, 20, 25);
            g2.fillOval(435, 145, 20, 25);

            // Sterne
            Random rng = new Random(42);
            g2.setColor(new Color(255,255,255,180));
            for (int i = 0; i < 60; i++) {
                int sx = rng.nextInt(800);
                int sy = rng.nextInt(200);
                float s = 1 + rng.nextFloat() * 2;
                g2.fillOval(sx, sy, (int)s, (int)s);
            }

            // Mond
            g2.setColor(new Color(255,240,200));
            g2.fillOval(680, 20, 60, 60);
            g2.setColor(new Color(15,5,40));
            g2.fillOval(700, 18, 60, 60);
        }

        private void drawArenaFloor(Graphics2D g2) {
            // Boden
            GradientPaint floor = new GradientPaint(0,380, new Color(30,20,60),
                                                     0,500, new Color(10,5,25));
            g2.setPaint(floor);
            g2.fillRect(0, 360, 800, 240);

            // Gitter-Linien
            g2.setColor(new Color(60,40,100,80));
            for (int x = 0; x < 800; x += 60) g2.drawLine(x,360, x+30,600);
            for (int y = 360; y < 600; y += 40) g2.drawLine(0,y, 800,y);

            // Trennlinie
            g2.setColor(new Color(120,80,200,100));
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(0, 360, 800, 360);
            g2.setStroke(new BasicStroke(1));
        }

        private void drawHPBar(Graphics2D g2, int x, int y, int hp, int maxHp, String name, Color col) {
            // Hintergrund
            g2.setColor(new Color(0,0,0,140));
            g2.fillRoundRect(x, y, 220, 36, 8, 8);

            // Leiste
            g2.setColor(new Color(40,20,20));
            g2.fillRoundRect(x+4, y+18, 212, 14, 4, 4);

            // Füllung
            int filled = (int)(212.0 * hp / maxHp);
            GradientPaint gp = new GradientPaint(x+4, y, col.darker(), x+4, y+32, col);
            g2.setPaint(gp);
            if (filled > 0)
                g2.fillRoundRect(x+4, y+18, filled, 14, 4, 4);

            // Rand
            g2.setColor(col.darker().darker());
            g2.drawRoundRect(x+4, y+18, 212, 14, 4, 4);

            // Name + HP
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            g2.setColor(Color.WHITE);
            g2.drawString(name, x+4, y+14);
            g2.setColor(col.brighter());
            g2.drawString(hp + "/" + maxHp, x+180, y+14);
        }

        private void drawProgressBar(Graphics2D g2) {
            // Schritt-Punkte
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            int startX = 120; int barY = 58; int spacing = 56;
            for (int i = 0; i < TOTAL_QUESTIONS; i++) {
                int bx = startX + i * spacing;
                if (i < questionIndex) {
                    g2.setColor(new Color(80,220,100));
                    g2.fillOval(bx, barY, 20, 20);
                    g2.setColor(new Color(40,120,60));
                    g2.drawString("✓", bx+3, barY+14);
                } else if (i == questionIndex) {
                    g2.setColor(new Color(220,180,60));
                    g2.fillOval(bx, barY, 20, 20);
                    g2.setColor(Color.BLACK);
                    g2.drawString("" + (i+1), bx + (i < 9 ? 5 : 3), barY+14);
                } else {
                    g2.setColor(new Color(60,40,100));
                    g2.fillOval(bx, barY, 20, 20);
                    g2.setColor(new Color(120,100,160));
                    g2.drawString("" + (i+1), bx + (i < 9 ? 5 : 3), barY+14);
                }
            }
        }

        private void drawQuestion(Graphics2D g2) {
            // Frage-Box
            g2.setColor(new Color(0,0,0,160));
            g2.fillRoundRect(160, 390, 480, 70, 16, 16);
            g2.setColor(new Color(140,80,220));
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(160, 390, 480, 70, 16, 16);
            g2.setStroke(new BasicStroke(1));

            String q = factor1 + "  ×  " + factor2 + "  =  ?";
            Font qf = new Font("SansSerif", Font.BOLD, 32);
            g2.setFont(qf);
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(new Color(255, 220, 80));
            g2.drawString(q, (800 - fm.stringWidth(q)) / 2, 435);

            // Zeit-Anzeige
            long elapsed = System.currentTimeMillis() - questionStartMs;
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2.setColor(elapsed < 3000 ? new Color(100,200,100) :
                        elapsed < 6000 ? new Color(220,180,50) : new Color(220,80,80));
            g2.drawString("� " + elapsed + " ms", 630, 410);
        }

        /** Pixelartiger Held */
        private void drawHero(Graphics2D g2, int cx, int cy) {
            // Schattenwurf
            g2.setColor(new Color(0,0,0,80));
            g2.fillOval(cx-24, cy+95, 48, 14);

            // Umhang
            g2.setColor(new Color(160,20,20));
            int[] capX = {cx-20, cx+20, cx+26, cx-26};
            int[] capY = {cy+30, cy+30, cy+88, cy+88};
            g2.fillPolygon(capX, capY, 4);

            // Körper
            g2.setColor(new Color(50,80,180));
            g2.fillRoundRect(cx-15, cy+28, 30, 45, 8, 8);
            // Rüstungsdetail
            g2.setColor(new Color(80,110,220));
            g2.fillRoundRect(cx-10, cy+32, 20, 10, 4, 4);

            // Kopf
            g2.setColor(new Color(255,210,170));
            g2.fillOval(cx-13, cy+2, 26, 26);

            // Helm
            g2.setColor(new Color(180,150,50));
            g2.fillArc(cx-14, cy-2, 28, 20, 0, 180);
            g2.fillRect(cx-16, cy+5, 32, 6);
            g2.setColor(new Color(220,190,80));
            g2.fillRect(cx-4, cy-4, 8, 6); // Helmkamm

            // Augen
            g2.setColor(Color.WHITE);
            g2.fillOval(cx-8, cy+10, 6, 6);
            g2.fillOval(cx+2, cy+10, 6, 6);
            g2.setColor(new Color(30,60,200));
            g2.fillOval(cx-6, cy+12, 4, 4);
            g2.fillOval(cx+4, cy+12, 4, 4);

            // Beine
            g2.setColor(new Color(35,35,70));
            g2.fillRoundRect(cx-12, cy+72, 10, 22, 4, 4);
            g2.fillRoundRect(cx+2,  cy+72, 10, 22, 4, 4);
            // Stiefel
            g2.setColor(new Color(60,35,10));
            g2.fillRoundRect(cx-13, cy+87, 12, 8, 2, 2);
            g2.fillRoundRect(cx+2,  cy+87, 12, 8, 2, 2);

            // Schwert animiert
            double swAng = heroAttacking ? -0.5 : Math.sin(tick * 0.06) * 0.15;
            Graphics2D sg = (Graphics2D) g2.create();
            sg.translate(cx + 16, cy + 45);
            sg.rotate(swAng);
            // Klinge
            sg.setColor(new Color(210,230,255));
            sg.fillRoundRect(0, -4, 55, 7, 4, 4);
            sg.setColor(new Color(160,190,230));
            sg.drawRoundRect(0, -4, 55, 7, 4, 4);
            // Glanz
            sg.setColor(new Color(255,255,255,200));
            sg.fillRoundRect(4, -2, 35, 2, 1, 1);
            // Griff
            sg.setColor(new Color(100,60,15));
            sg.fillRoundRect(-12, -4, 12, 8, 3, 3);
            sg.setColor(new Color(200,160,40));
            sg.fillRoundRect(-4, -8, 7, 16, 2, 2); // Parierstange
            sg.dispose();

            // Schild
            g2.setColor(new Color(190,140,30));
            int[] shX = {cx-22, cx-36, cx-36, cx-22};
            int[] shY = {cy+26, cy+30, cy+60, cy+64};
            g2.fillPolygon(shX, shY, 4);
            g2.setColor(new Color(150,100,10));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawPolygon(shX, shY, 4);
            g2.setStroke(new BasicStroke(1));
            g2.setColor(new Color(220,50,50));
            g2.fillOval(cx-34, cy+41, 8, 8);
        }

        /** Monster je nach Index anders aussehend */
        private void drawEnemy(Graphics2D g2, int cx, int cy, int type) {
            Color col = ENEMY_COLORS[type];
            // Schatten
            g2.setColor(new Color(0,0,0,80));
            g2.fillOval(cx-28, cy+95, 56, 14);

            float bob = (float) Math.sin(tick * 0.08 + type) * 4f;
            int bcy = cy + (int) bob;

            switch (type % 5) {
                case 0: drawGoblin(g2, cx, bcy, col);  break;
                case 1: drawSkeleton(g2, cx, bcy, col); break;
                case 2: drawTroll(g2, cx, bcy, col);   break;
                case 3: drawWerewolf(g2, cx, bcy, col); break;
                case 4: drawDragon(g2, cx, bcy, col);  break;
            }

            // Name des Gegners
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            g2.setColor(col.brighter());
            FontMetrics fm = g2.getFontMetrics();
            int nx = cx - fm.stringWidth(ENEMY_NAMES[type]) / 2;
            g2.drawString(ENEMY_NAMES[type], nx, bcy - 10);
        }

        private void drawGoblin(Graphics2D g2, int cx, int cy, Color col) {
            // Körper
            g2.setColor(col);
            g2.fillOval(cx-14, cy+20, 28, 40);
            // Kopf
            g2.setColor(col.darker());
            g2.fillOval(cx-18, cy-5, 36, 34);
            // Ohren
            g2.fillOval(cx-28, cy+2, 14, 10);
            g2.fillOval(cx+14, cy+2, 14, 10);
            // Augen
            g2.setColor(Color.YELLOW);
            g2.fillOval(cx-10, cy+6, 8, 8);
            g2.fillOval(cx+2,  cy+6, 8, 8);
            g2.setColor(Color.BLACK);
            g2.fillOval(cx-8,  cy+8, 4, 4);
            g2.fillOval(cx+4,  cy+8, 4, 4);
            // Mund
            g2.setColor(new Color(180,60,60));
            g2.drawArc(cx-8, cy+18, 16, 8, 180, 180);
            // Keule
            g2.setColor(new Color(100,60,20));
            g2.fillRoundRect(cx+20, cy+10, 8, 50, 4, 4);
            g2.setColor(new Color(80,50,15));
            g2.fillOval(cx+16, cy+5, 16, 16);
            // Beine
            g2.setColor(col.darker());
            g2.fillRoundRect(cx-12, cy+58, 10, 20, 4,4);
            g2.fillRoundRect(cx+2,  cy+58, 10, 20, 4,4);
        }

        private void drawSkeleton(Graphics2D g2, int cx, int cy, Color col) {
            // Knochen-Körper
            g2.setColor(col);
            g2.fillRoundRect(cx-10, cy+22, 20, 40, 4, 4);
            // Rippen
            g2.setColor(col.darker());
            for (int i = 0; i < 4; i++) {
                g2.drawLine(cx-10, cy+28+i*8, cx+10, cy+28+i*8);
            }
            // Schädel
            g2.setColor(col);
            g2.fillOval(cx-15, cy, 30, 28);
            // Kinnlade
            g2.fillRoundRect(cx-12, cy+22, 24, 12, 2, 2);
            // Augen (Augenhöhlen)
            g2.setColor(Color.BLACK);
            g2.fillOval(cx-10, cy+8, 8, 8);
            g2.fillOval(cx+2,  cy+8, 8, 8);
            // Zähne
            g2.setColor(col);
            for (int i = 0; i < 4; i++)
                g2.fillRect(cx-9+i*5, cy+26, 3, 5);
            // Arme
            g2.setColor(col);
            g2.fillRoundRect(cx-24, cy+24, 14, 6, 2,2);
            g2.fillRoundRect(cx+10, cy+24, 14, 6, 2,2);
            // Beine
            g2.fillRoundRect(cx-10, cy+60, 8, 22, 2,2);
            g2.fillRoundRect(cx+2,  cy+60, 8, 22, 2,2);
        }

        private void drawTroll(Graphics2D g2, int cx, int cy, Color col) {
            cy += 10; // Troll ist größer & sitzt tiefer
            // massiger Körper
            g2.setColor(col);
            g2.fillRoundRect(cx-28, cy+18, 56, 56, 10, 10);
            // Kopf
            g2.setColor(col.darker());
            g2.fillOval(cx-22, cy-12, 44, 36);
            // Horn
            g2.setColor(new Color(180,140,50));
            int[] hx = {cx-4, cx+4, cx};
            int[] hy = {cy-12, cy-12, cy-32};
            g2.fillPolygon(hx, hy, 3);
            // Augen
            g2.setColor(new Color(255,60,0));
            g2.fillOval(cx-10, cy+2, 9, 9);
            g2.fillOval(cx+2,  cy+2, 9, 9);
            g2.setColor(Color.BLACK);
            g2.fillOval(cx-8, cy+4, 5, 5);
            g2.fillOval(cx+4, cy+4, 5, 5);
            // Mund
            g2.setColor(new Color(150,40,40));
            g2.fillRoundRect(cx-10, cy+15, 20, 8, 4,4);
            g2.setColor(new Color(220,220,180));
            g2.fillRect(cx-7, cy+15, 4, 5);
            g2.fillRect(cx+3, cy+15, 4, 5);
            // Arme
            g2.setColor(col);
            g2.fillRoundRect(cx-46, cy+22, 20, 14, 6,6);
            g2.fillRoundRect(cx+26, cy+22, 20, 14, 6,6);
            // Beine
            g2.fillRoundRect(cx-22, cy+72, 16, 18, 4,4);
            g2.fillRoundRect(cx+6,  cy+72, 16, 18, 4,4);
        }

        private void drawWerewolf(Graphics2D g2, int cx, int cy, Color col) {
            // Körper
            g2.setColor(col);
            g2.fillRoundRect(cx-16, cy+22, 32, 46, 8,8);
            // Fell-Textur
            g2.setColor(col.darker());
            for (int i=0;i<6;i++) g2.fillOval(cx-12+i*5, cy+24, 4, 6);
            // Kopf (Schnauze)
            g2.fillOval(cx-16, cy-5, 32, 30);
            g2.fillOval(cx-8, cy+12, 16, 14); // Schnauze
            // Ohren
            int[] ex = {cx-20, cx-10, cx-16}; int[] ey={cy-5, cy-5, cy-22};
            g2.fillPolygon(ex, ey, 3);
            int[] ex2={cx+10,cx+20,cx+16}; int[] ey2={cy-5,cy-5,cy-22};
            g2.fillPolygon(ex2,ey2,3);
            // Augen
            g2.setColor(new Color(255,140,0));
            g2.fillOval(cx-10, cy+4, 7, 7);
            g2.fillOval(cx+3, cy+4, 7, 7);
            g2.setColor(Color.BLACK);
            g2.fillOval(cx-8,cy+6,3,3); g2.fillOval(cx+5,cy+6,3,3);
            // Nase
            g2.setColor(Color.BLACK);
            g2.fillOval(cx-3, cy+16, 6, 4);
            // Klauen
            g2.setColor(col);
            g2.fillRoundRect(cx-28,cy+28,14,8,4,4);
            g2.fillRoundRect(cx+14,cy+28,14,8,4,4);
            g2.setColor(new Color(220,220,180));
            for (int i=0;i<3;i++) { g2.drawLine(cx-27+i*3,cy+28,cx-30+i*3,cy+23); }
            for (int i=0;i<3;i++) { g2.drawLine(cx+15+i*3,cy+28,cx+13+i*3,cy+23); }
            // Beine
            g2.setColor(col);
            g2.fillRoundRect(cx-13,cy+66,11,22,4,4);
            g2.fillRoundRect(cx+2,cy+66,11,22,4,4);
        }

        private void drawDragon(Graphics2D g2, int cx, int cy, Color col) {
            cy -= 10; // Drache ist größer
            // Flügel links
            g2.setColor(col.darker());
            int[] wlx = {cx-10, cx-60, cx-50, cx-10};
            int[] wly = {cy+20, cy+0, cy+50, cy+55};
            g2.fillPolygon(wlx, wly, 4);
            // Flügel rechts
            int[] wrx = {cx+10, cx+60, cx+50, cx+10};
            int[] wry = {cy+20, cy+0, cy+50, cy+55};
            g2.fillPolygon(wrx, wry, 4);
            // Körper
            g2.setColor(col);
            g2.fillOval(cx-22, cy+20, 44, 55);
            // Hals
            g2.fillRoundRect(cx-10, cy-10, 20, 34, 10,10);
            // Kopf
            g2.fillOval(cx-18, cy-28, 36, 28);
            g2.fillOval(cx-8, cy-10, 16, 16); // Schnauze
            // Hörner
            int[] hx1={cx-12,cx-7,cx-14}; int[] hy1={cy-28,cy-28,cy-48};
            g2.setColor(new Color(180,120,30)); g2.fillPolygon(hx1,hy1,3);
            int[] hx2={cx+7,cx+12,cx+14}; int[] hy2={cy-28,cy-28,cy-48};
            g2.fillPolygon(hx2,hy2,3);
            // Augen
            g2.setColor(new Color(255,80,0));
            g2.fillOval(cx-12,cy-22,9,9); g2.fillOval(cx+3,cy-22,9,9);
            g2.setColor(Color.BLACK);
            g2.fillOval(cx-10,cy-20,5,5); g2.fillOval(cx+5,cy-20,5,5);
            // Flamme
            if (tick % 40 < 20) {
                g2.setColor(new Color(255,100,0,200));
                g2.fillOval(cx-20, cy-8, 40, 14);
                g2.setColor(new Color(255,200,0,150));
                g2.fillOval(cx-14, cy-5, 28, 8);
            }
            // Schwanz
            g2.setColor(col);
            g2.drawArc(cx+10, cy+50, 40, 30, 0, 270);
            // Beine
            g2.fillRoundRect(cx-18,cy+73,13,18,4,4);
            g2.fillRoundRect(cx+5,cy+73,13,18,4,4);
        }
    }
}
