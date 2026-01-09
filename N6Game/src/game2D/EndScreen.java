package game2D;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Random;

public class EndScreen {
    private float alpha = 0.5f; // Controls fade-in effect
    private ArrayList<Star> stars = new ArrayList<>();
    private int tickCount = 0;
    private Random random = new Random();

    public EndScreen() {
        for (int i = 0; i < 100; i++) {
            stars.add(new Star());
        }
    }

    public void update() {
        tickCount++;
        if (alpha < 1.0f) alpha += 0.02f; // Smooth fade-in
        for (Star star : stars) star.update();
    }

    public void EndScreenGraphics(Graphics2D g, int getWidth, int getHeight) {
        AffineTransform originalTransform = g.getTransform();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background gradient
        g.setPaint(new GradientPaint(0, 0, new Color(0, 0, 40), 0, getHeight, Color.BLACK));
        g.fillRect(0, 0, getWidth, getHeight);

        // Draw stars
        for (Star star : stars) star.draw(g, getWidth, getHeight);

        // Draw "Game Over" text with fading effect
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        drawCenteredText(g, "GAME OVER", getWidth / 2, getHeight / 2 - 50, new Font("Press Start 2P", Font.BOLD, 100), Color.RED);

        // Pulsating "Press ESC to Exit" message
        float pulseAlpha = 0.6f + 0.4f * (float) Math.sin(tickCount * 0.1);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pulseAlpha));
        drawCenteredText(g, "Press ESC to Exit", getWidth / 2, getHeight / 2 + 50, new Font("Press Start 2P", Font.PLAIN, 20), Color.WHITE);

        g.setTransform(originalTransform);
    }

    private void drawCenteredText(Graphics2D g, String text, int centerX, int centerY, Font font, Color color) {
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics(font);
        int x = centerX - metrics.stringWidth(text) / 2;
        int y = centerY - metrics.getHeight() / 2 + metrics.getAscent();
        g.setColor(color);
        g.drawString(text, x, y);
    }

    private class Star {
        private double x, y, speed;
        private float brightness;
        private int size;

        public Star() {
            reset();
            x = random.nextDouble();
            y = random.nextDouble();
        }

        public void reset() {
            x = random.nextDouble();
            y = 0;
            speed = 0.002 + random.nextDouble() * 0.003;
            brightness = 0.5f + random.nextFloat() * 0.5f;
            size = 2 + random.nextInt(3);
        }

        public void update() {
            y += speed;
            if (y > 1.0) reset();
        }

        public void draw(Graphics2D g, int maxWidth, int maxHeight) {
            int drawX = (int) (x * maxWidth);
            int drawY = (int) (y * maxHeight);
            g.setColor(new Color(1f, 1f, 1f, brightness * alpha));
            g.fillOval(drawX, drawY, size, size);
        }
    }
}
