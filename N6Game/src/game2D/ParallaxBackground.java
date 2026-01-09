package game2D;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class ParallaxBackground {

    // Represents a single image in the parallax background
    private class Layer {
        BufferedImage image;
        float speed;  // How fast this layer moves 
        float x, y;   // Position of the layer
        float scale;  // Scale factor for resizing the image

        public Layer(BufferedImage img, float speed, float y, float scale) {
            this.image = img;
            this.speed = speed;
            this.x = 0;
            this.y = y;
            this.scale = scale;
        }
    }

    private ArrayList<Layer> layers = new ArrayList<>();
    private int screenWidth, screenHeight;
    private float defaultScale = 2.5f;  
    private float lastPlayerX;
    private float cameraX = 0;
    private float cameraY = 0;

    public ParallaxBackground(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.lastPlayerX = 0;
    }

    // Add a layer with default scaling
    public void addLayer(BufferedImage image, float speed, float yPosition) {
        addLayer(image, speed, yPosition, defaultScale);
    }

    // Add a layer with custom scaling
    public void addLayer(BufferedImage image, float speed, float yPosition, float scale) {
        layers.add(new Layer(image, speed, yPosition, scale));
    }

    // Load an image from file path
    public BufferedImage loadBufferedImage(String path) {
        try {
            Image img = Toolkit.getDefaultToolkit().getImage(path);
            MediaTracker tracker = new MediaTracker(new Canvas());
            tracker.addImage(img, 0);
            tracker.waitForAll();

            BufferedImage bImage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = bImage.createGraphics();
            g2.drawImage(img, 0, 0, null);
            g2.dispose();

            return bImage;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Sets the camera position and updates the layers
    public void setCameraOffset(float cameraX, float cameraY) {
        this.cameraX = cameraX;
        this.cameraY = cameraY;
        update(cameraX);
    }

    // Adjusts layer positions based on player's movement
    public void update(float playerX) {
        float deltaX = playerX - lastPlayerX;  // How much the player moved since the last frame
        lastPlayerX = playerX;

        for (Layer layer : layers) {
            layer.x -= deltaX * layer.speed;  // Move layer based on its speed

            float scaledWidth = layer.image.getWidth() * layer.scale;

            // Wrap the layer around to create a continuous scrolling effect
            if (layer.x <= -scaledWidth) layer.x += scaledWidth;
            if (layer.x >= scaledWidth) layer.x -= scaledWidth;
        }
    }

    // Draws all the background layers with parallax effect
    public void draw(Graphics2D g) {
        for (Layer layer : layers) {
            int imgWidth = (int) (layer.image.getWidth() * layer.scale);
            int imgHeight = (int) (layer.image.getHeight() * layer.scale);

            int startX = (int) layer.x % imgWidth;
            if (startX > 0) startX -= imgWidth;

            for (int x = startX; x < screenWidth; x += imgWidth) {
                g.drawImage(layer.image, (int)(x - cameraX * layer.speed), (int) layer.y, imgWidth, imgHeight, null);
            }
        }
    }
}
