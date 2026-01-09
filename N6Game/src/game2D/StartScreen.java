package game2D;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class StartScreen {

    private float alpha = 0.8f; // Increased alpha for better star visibility
    private ArrayList<Star> stars = new ArrayList<>();
    private Random random = new Random();

    public StartScreen() {
        // Initialize stars
        for (int i = 0; i < 100; i++) {
            stars.add(new Star());
        }
    }

    /**
     * Renders the start screen for the game.
     *
     * @param g             Graphics object used for drawing.
     * @param currentWidth  Current screen width.
     * @param currentHeight Current screen height.
     */
    public void render(Graphics g, int currentWidth, int currentHeight) {
        Graphics2D g2d = (Graphics2D) g.create();

        try {
            // Enable smooth rendering for better visuals
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            // Background gradient from dark blue to black
            g2d.setPaint(new GradientPaint(0, 0, new Color(0, 0, 40), 0, currentHeight, Color.BLACK));
            g2d.fillRect(0, 0, currentWidth, currentHeight);

            // Update and draw moving stars
            for (Star star : stars) {
                star.update();
                star.draw(g2d, currentWidth, currentHeight);
            }

            // Title
            String gameTitle = "AWAKENED";
            Font titleFont = new Font("Arial Black", Font.ITALIC, Math.min(currentWidth / 9, currentHeight / 7));
            g2d.setFont(titleFont);
            FontMetrics fmTitle = g2d.getFontMetrics();
            int titleX = (currentWidth - fmTitle.stringWidth(gameTitle)) / 2;
            int titleY = currentHeight / 4 + fmTitle.getAscent() / 2;

            // Title shadow for better visibility
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.drawString(gameTitle, titleX + 4, titleY + 4);

            // Main title text
            g2d.setColor(new Color(173, 216, 230));
            g2d.drawString(gameTitle, titleX, titleY);

            // Tagline
            String tagline = "The dead walk again...";
            g2d.setFont(new Font("Georgia", Font.ITALIC, Math.min(currentWidth / 35, currentHeight / 30)));
            FontMetrics fmTagline = g2d.getFontMetrics();
            int taglineX = (currentWidth - fmTagline.stringWidth(tagline)) / 2;
            int taglineY = titleY + fmTagline.getHeight() + 10;

            g2d.setColor(new Color(160, 180, 200));
            g2d.drawString(tagline, taglineX, taglineY);

            // Start Prompt
            String startPrompt = "ENTER to Start";
            g2d.setFont(new Font("Arial", Font.PLAIN, Math.min(currentWidth / 28, currentHeight / 23)));
            FontMetrics fmPrompt = g2d.getFontMetrics();
            int promptX = (currentWidth - fmPrompt.stringWidth(startPrompt)) / 2;
            int promptY = currentHeight * 3 / 4;

            g2d.setColor(Color.WHITE);
            g2d.drawString(startPrompt, promptX, promptY);

            // Controls Section
            int controlsX = 40;
            int controlsY = currentHeight - (currentHeight / 5) - 100;
            int lineSpacing = Math.max(18, currentHeight / 45);

            // Controls title
            g2d.setFont(new Font("Arial", Font.BOLD, Math.min(currentWidth / 40, currentHeight / 35)));
            g2d.setColor(new Color(173, 216, 230));
            g2d.drawString("CONTROLS:", controlsX, controlsY);

            // Control instructions
            controlsY += lineSpacing * 1.5;
            g2d.setFont(new Font("Consolas", Font.PLAIN, Math.max(9, Math.min(currentWidth / 70, currentHeight / 60))));
            g2d.setColor(new Color(210, 210, 220));

            String[] controlLines = {
                "Up / Space   : Jump",
                "Left         : Walk Left",
                "Right        : Walk Right",
                "B / L-Click  : Debug Mode",
                "S / R-Click  : Shoot",
                "Esc          : Close",
                "R            : Restart"
            };

            for (String line : controlLines) {
                g2d.drawString(line, controlsX, controlsY);
                controlsY += lineSpacing;
            }

        } finally {
            g2d.dispose();
        }
    }

    private class Star {
        private double x, y, speed;
        private float brightness;
        private int size;

        public Star() {
            reset();
        }

        public void reset() {
            x = random.nextDouble(); // Random X position
            y = random.nextDouble(); // Start randomly anywhere on screen
            speed = 0.002 + random.nextDouble() * 0.005;
            brightness = 0.6f + random.nextFloat() * 0.01f; // Ensure some stars are bright
            size = 2 + random.nextInt(3); // Keep size range
        }

        public void update() {
            y += speed;
            if (y > 1.0) reset(); // Reset when off-screen
        }

        public void draw(Graphics2D g, int maxWidth, int maxHeight) {
            int drawX = (int) (x * maxWidth);
            int drawY = (int) (y * maxHeight);
            g.setColor(new Color(1f, 1f, 1f, brightness)); // Ensures visibility
            g.fillOval(drawX, drawY, size, size);
        }
    }
}
