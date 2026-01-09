
// Import Statements
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.FloatControl;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.event.MouseEvent;

import game2D.*;

// Game demonstrates how we can override the GameCore class
// to create our own 'game'. We usually need to implement at
// least 'draw' and 'update' (not including any local event handling)
// to begin the process. You should also add code to the 'init'
// method that will initialise event handlers etc. 

// Student ID: 3456916

@SuppressWarnings("serial")

public class Game extends GameCore implements MouseListener {

    // Useful game constants
    static int screenWidth = 512;
    static int screenHeight = 384;

    // Game state flags
    boolean gameStarted = false;
    boolean gameEnd = false;

    // Player movement states
    boolean jumping = false;
    boolean moveLeft = false;
    boolean moveRight = false;
    boolean shoot = false;
    boolean isOnGround = false;
    boolean facingRight = true;

    // Debugging flag
    boolean debug = true; // Enables debugging mode for additional visuals

    // Player death and animation states
    boolean dead = false;
    boolean playingDeathAnimation = false;
    long deathAnimationStartTime = 0;
    long deathAnimationDuration = 3000; // Time for death animation in millisecons

    // Money counter display
    boolean showMoneyCounter = false;
    int moneyCounterValue = 0;
    long moneyCounterStartTime = 0;
    long moneyCounterDuration = 1000;
    float moneyCounterX = 0; // X-coordinate for money counter display
    float moneyCounterY = 0; // Y-coordinate for money counter display

    // Level completion state
    boolean levelCompleted = false;
    long levelCompletedTime = 0;
    long levelCompletedDuration = 3000; // Duration to show level completed message (ms)
    int currentLevel = 1; // Tracks the current level the player is in

    // Level transition messages
    boolean showingLevelMessage = false;
    long levelMessageStartTime = 0;
    long levelMessageDuration = 2000; // Duration the level message is displayed (ms)

    // Player stats
    int playerLives = 3; // Set minimum players lives
    int playerMoney = 0; // Set default palyer money

    // Player animations
    Animation heartAnim;
    Animation moneyAnim;

    // Player invincibility mechanics
    long invincibilityTime = 0;
    long invincibilityDuration = 5000;
    boolean isInvincible = false;

    // Physics and movement properties
    float gravity = 0.002f;
    float jumpVelocity = -0.7f;
    float maxFallSpeed = 0.6f;
    float moveSpeed = 0.01f;
    float bulletSpeed = 0.5f;

    // Shooting mechanics
    long lastShotTime = 0;
    long shootCooldown = 500;

    // Player animations
    Animation walkRight;
    Animation walkLeft;
    Animation idleRight;
    Animation idleLeft;
    Animation jumpRight;
    Animation jumpLeft;
    Animation shootRight;
    Animation shootLeft;
    Animation bulletAnim;
    Animation deathAnim;

    // Zombie animations (indexed for different zombie types)
    Animation[] zombieIdleRight = new Animation[4]; // Idle animations for zombies facing right
    Animation[] zombieIdleLeft = new Animation[4]; // Idle animations for zombies facing left
    Animation[] zombieWalkRight = new Animation[4]; // Walking animations for zombies facing right
    Animation[] zombieWalkLeft = new Animation[4]; // Walking animations for zombies facing left
    Animation[] zombieDeath = new Animation[4]; // Death animations for zombies

    // Lists that manage zombies' spawning, movement, and death states
    ArrayList<Sprite> zombies = new ArrayList<Sprite>();
    ArrayList<Integer> zombieTypes = new ArrayList<Integer>();
    ArrayList<Sprite> dyingZombies = new ArrayList<Sprite>();
    ArrayList<Long> zombieDeathTimes = new ArrayList<Long>();
    ArrayList<Integer> dyingZombieTypes = new ArrayList<Integer>();
    ArrayList<Boolean> zombieAlerted = new ArrayList<Boolean>();

    float zombieSpeed = 0.004f; // Zombies movement speed
    float zombieDetectionRange = 300f; // The distance at which zombies go after the palyer
    Random random = new Random(); // Random used for spawning zombie types

    // Audio
    private HashMap<String, Clip> audioClips = new HashMap<>();

    // Money Drops
    ArrayList<Sprite> moneyDrops = new ArrayList<Sprite>();
    ArrayList<Float> moneyVelocities = new ArrayList<Float>();
    ArrayList<Long> moneyDropTimes = new ArrayList<Long>();

    long moneyLifespan = 10000;
    float moneyJumpVelocity = -0.5f;

    // Game Objects
    Sprite player = null;
    ArrayList<Sprite> clouds = new ArrayList<Sprite>();
    ArrayList<Sprite> bullets = new ArrayList<Sprite>();
    ArrayList<Tile> collidedTiles = new ArrayList<Tile>();

    // Tile Map & Parallax Background
    TileMap tmap = new TileMap(); // Our tile map, note that we load it in init()
    long total; // The score will be the total time elapsed since a crash

    ParallaxBackground parallaxBg;
    ReverbAudioPlayer reverbAudioPlayer;

    // Background Music
    private String level1MusicPath = "sounds/level1.mid";
    private String level2MusicPath = "sounds/level2.mid";
    private String gameOverSoundPath = "sounds/game_over.wav";
    private Clip level1MusicClip;
    private Clip level2MusicClip;
    private Clip gameOverSoundClip;
    private Clip currentLevelMusicClip;

    private boolean isFadingIn = false;
    private boolean isFadingOut = false;
    private long fadeStartTime = 0;
    private long fadeInDuration = 2000;
    private long fadeOutDuration = 2000;
    private float currentVolume = 0.0f;
    private Timer fadeTimer;

    /**
     * The obligatory main method that creates
     * an instance of our class and starts it running
     * 
     * @param args The list of parameters this program might use (ignored)
     */

    public static void main(String[] args) {
        Game gct = new Game();
        gct.init();

        // Start in windowed mode with the given screen height and width
        gct.run(false, screenWidth, screenHeight);
    }

    // --------------------------------------------------------------------------------------------
    // Music
    // --------------------------------------------------------------------------------------------

    public void loadAudio(String path, String name) {
        try {
            File audioFile = new File(path); // Load the audio file
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile); // Get an audio stream
            Clip clip = AudioSystem.getClip(); // Create a new audio clip
            clip.open(audioStream);
            audioClips.put(name, clip); // Store the clip for later playback
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error loading audio: " + path);
            e.printStackTrace();
        }
    }

    public Clip getAudioClip(String name) {
        return audioClips.get(name);
    }

    public void playAudio(Clip clip) {
        if (clip != null) {
            clip.setFramePosition(0); // Reset playback to the beginning
            clip.start(); // Start playing the clip
        }
    }

    public void playAudioFile(String path) {
        try {
            File audioFile = new File(path); // Load the audio file
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile); // Convert to audio stream
            Clip clip = AudioSystem.getClip(); // Create an audio clip
            clip.open(audioStream); // Open the stream in the clip
            clip.start(); // Play the audio immediately
        }

        catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error playing audio: " + path);
            e.printStackTrace();
        }
    }

    public void loopAudio(Clip clip) {
        if (clip != null) {
            clip.setFramePosition(0); // Restart from the beginning
            clip.loop(Clip.LOOP_CONTINUOUSLY); // Loop indefinitely
        }
    }

    public void stopAudio(Clip clip) {
        if (clip != null && clip.isRunning()) {
            clip.stop(); // Stop playback if it's running
        }
    }

    public void setClipVolume(Clip clip, float volume) {
        if (clip != null) {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN); // Get volume
                                                                                                      // control
            float dB = (volume <= 0.0f) ? -80.0f : (float) (20.0 * Math.log10(volume)); // Convert volume to dB
            gainControl.setValue(Math.max(-80.0f, Math.min(6.0f, dB))); // Clamp volume between -80 and 6 dB
        }
    }

    // Adds fade in sound filter to the audio clip
    public void fadeInMusic(final Clip clip) {
        if (clip != null) {
            isFadingIn = true; // Indicate that fade-in is starting
            isFadingOut = false;
            fadeStartTime = System.currentTimeMillis();
            currentVolume = 0.0f; // Start at minimum volume

            setClipVolume(clip, currentVolume);
            clip.setFramePosition(0);
            clip.loop(Clip.LOOP_CONTINUOUSLY); // Play the audio in a loop

            if (fadeTimer != null) {
                fadeTimer.cancel(); // Stop any ongoing fade timer
            }

            fadeTimer = new Timer();
            fadeTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (isFadingIn) {
                        long elapsedTime = System.currentTimeMillis() - fadeStartTime; // Calculate elapsed fade time

                        if (elapsedTime >= fadeInDuration) {
                            setClipVolume(clip, 1.0f);
                            isFadingIn = false;
                            this.cancel();
                        } else {
                            setClipVolume(clip, (float) elapsedTime / fadeInDuration); // Increase volume gradually
                        }
                    } else {
                        this.cancel(); // Stop if fading is not active
                    }
                }
            }, 0, 50); // Update volume every 50ms
        }
    }

    // Adds fade out sound filter to the audio clip
    public void fadeOutMusic(final Clip clip) {

        // Check if there is no audio clip palying currently
        if (clip != null && clip.isRunning()) {
            isFadingOut = true;
            isFadingIn = false;
            fadeStartTime = System.currentTimeMillis(); // Capture the time when fading starts
            currentVolume = 1.0f; // Start at full volume

            // Stop any audio currently palying if any audio is playing
            if (fadeTimer != null) {
                fadeTimer.cancel();
            }

            // Create a new timer for handling the fade-out effect over time
            fadeTimer = new Timer();
            fadeTimer.scheduleAtFixedRate(new TimerTask() {

                @Override
                public void run() {
                    if (isFadingOut) {
                        long currentTime = System.currentTimeMillis();
                        long elapsedTime = currentTime - fadeStartTime;

                        if (elapsedTime >= fadeOutDuration) {
                            currentVolume = 0.0f;
                            setClipVolume(clip, currentVolume); // Mute the clip
                            stopAudio(clip); // Stop playback
                            isFadingOut = false; // Mark the fade-out as complete
                            this.cancel(); // Stop the timer task
                        }

                        else {

                            // Gradually reduce the volume based on elapsed time
                            currentVolume = 1.0f - ((float) elapsedTime / fadeOutDuration);
                            setClipVolume(clip, currentVolume);
                        }
                    } else {
                        this.cancel();
                    }
                }
            }, 0, 50);
        }
    }

    // --------------------------------------------------------------------------------------------
    // Init / Main
    // --------------------------------------------------------------------------------------------

    /**
     * Initialise the class, e.g. set up variables, load images,
     * create animations, register event handlers.
     * 
     * This shows you the general principles but you should create specific
     * methods for setting up your game that can be called again when you wish to
     * restart the game (for example you may only want to load animations once
     * but you could reset the positions of sprites each time you restart the game).
     */
    public void init() {
        Sprite s; // Temporary reference to the sprite

        // Change the window title
        this.setTitle("🧟‍♂️ AWAKENED 🧟‍♂️");
        // Close everything when the X button is presed
        this.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);

        // Loading the level as well as the tile map
        loadLevel(1);
        setSize(tmap.getPixelWidth() / 4, tmap.getPixelHeight());
        setVisible(true);

        // Create a set of background sprites that we can
        // rearrange to give the illusion of motion
        // landing = new Animation();
        // landing.loadAnimationFromSheet("images/landbird.png", 4, 1, 60);

        // landing = new Animation();
        // landing.loadAnimationFromSheet("images/landbird.png", 4, 1, 60);

        // // Initialise the player with an animation
        // player = new Sprite(landing);

        // // Load a single cloud animation
        // Animation ca = new Animation();
        // ca.addFrame(loadImage("images/cloud.png"), 1000);

        // // Create 3 clouds at random positions off the screen
        // // to the right
        // for (int c=0; c<3; c++)
        // {
        // s = new Sprite(ca);
        // s.setX(screenWidth + (int)(Math.random()*200.0f));
        // s.setY(30 + (int)(Math.random()*150.0f));
        // s.setVelocityX(-0.02f);
        // s.show();
        // clouds.add(s);
        // }

        // -------------------- Parallax Background ----------------------------
        parallaxBg = new ParallaxBackground(screenWidth, screenHeight);
        parallaxBg.addLayer(parallaxBg.loadBufferedImage("images/background/1.png"), 0.2f, 0);
        parallaxBg.addLayer(parallaxBg.loadBufferedImage("images/background/2.png"), 0.2f, 0);
        parallaxBg.addLayer(parallaxBg.loadBufferedImage("images/background/3.png"), 0.01f, 0);
        parallaxBg.addLayer(parallaxBg.loadBufferedImage("images/background/4.png"), 0.2f, 0);
        parallaxBg.addLayer(parallaxBg.loadBufferedImage("images/background/5.png"), 0.01f, 0);

        loadAudio(level1MusicPath, "level1");
        loadAudio(level2MusicPath, "level2");
        loadAudio(gameOverSoundPath, "gameOver");

        level1MusicClip = getAudioClip("level1");
        level2MusicClip = getAudioClip("level2");
        gameOverSoundClip = getAudioClip("gameOver");

        // -------------------- Player Animations ----------------------------
        walkRight = new Animation();
        walkRight = new Animation();
        walkRight.loadAnimationFromSheet("images/Soldier/Run_Right.png", 6, 1, 150);

        walkLeft = new Animation();
        walkLeft.loadAnimationFromSheet("images/Soldier/Run_Left.png", 6, 1, 150);

        idleRight = new Animation();
        idleRight.loadAnimationFromSheet("images/Soldier/Idle_Right.png", 6, 1, 100);

        idleLeft = new Animation();
        idleLeft.loadAnimationFromSheet("images/Soldier/Idle_Left.png", 6, 1, 100);

        shootRight = new Animation();
        shootRight.loadAnimationFromSheet("images/Soldier/Shoot_Right.png", 4, 1, 100);

        shootLeft = new Animation();
        shootLeft.loadAnimationFromSheet("images/Soldier/Shoot_Left.png", 4, 1, 100);

        deathAnim = new Animation();
        deathAnim.loadAnimationFromSheet("images/Soldier/Dead.png", 6, 1, 100);
        deathAnim.setLoop(false);

        bulletAnim = new Animation();
        bulletAnim.addFrame(loadImage("images/Soldier/Bullet.png"), 1000);

        heartAnim = new Animation();
        heartAnim.addFrame(loadImage("images/Soldier/Heart.png"), 1000);

        moneyAnim = new Animation();
        moneyAnim.loadAnimationFromSheet("images/money.png", 6, 1, 100);

        // -------------------- Zombie Animations ----------------------------
        for (int i = 1; i <= 4; i++) {
            zombieIdleRight[i - 1] = new Animation();
            zombieIdleRight[i - 1].loadAnimationFromSheet("images/Zombies/Zombie_" + i + "/Idle_Right.png", 6, 1, 150);

            zombieIdleLeft[i - 1] = new Animation();
            zombieIdleLeft[i - 1].loadAnimationFromSheet("images/Zombies/Zombie_" + i + "/Idle_Left.png", 6, 1, 150);

            zombieWalkRight[i - 1] = new Animation();
            zombieWalkRight[i - 1].loadAnimationFromSheet("images/Zombies/Zombie_" + i + "/Walk_Right.png", 6, 1, 100);

            zombieWalkLeft[i - 1] = new Animation();
            zombieWalkLeft[i - 1].loadAnimationFromSheet("images/Zombies/Zombie_" + i + "/Walk_Left.png", 6, 1, 100);

            zombieDeath[i - 1] = new Animation();
            zombieDeath[i - 1].loadAnimationFromSheet("images/Zombies/Zombie_" + i + "/Dead.png", 5, 1, 500);
        }

        player = new Sprite(idleRight);

        initialiseGame();

        System.out.println(tmap);

        // Awaiting user mouse events
        addMouseListener(this);
    }

    /**
     * Loads the required level and the respecitve level music
     * 
     * @param level The interger value of the current level the player should play.
     */
    private void loadLevel(int level) {

        if (level == 1) {

            // Load map from text file
            tmap.loadMap("maps", "Level_One_Map.txt");
            currentLevel = 1;

            // Set previous music
            if (currentLevelMusicClip != null) {
                fadeOutMusic(currentLevelMusicClip);
            }

            // Set current level music
            currentLevelMusicClip = level1MusicClip;
            fadeInMusic(currentLevelMusicClip);

        } else if (level == 2) {

            // Load map from text file
            tmap.loadMap("maps", "Level_Two_Map.txt");
            currentLevel = 2;

            // Remove previous music
            if (currentLevelMusicClip != null) {
                fadeOutMusic(currentLevelMusicClip);
            }

            // Set current level music
            currentLevelMusicClip = level2MusicClip;
            fadeInMusic(currentLevelMusicClip);
        }

        showingLevelMessage = true;
        levelMessageStartTime = System.currentTimeMillis();
    }

    /**
     * You will probably want to put code to restart a game in
     * a separate method so that you can call it when restarting
     * the game when the player loses.
     * 
     * This method loads all the important game elements
     */
    public void initialiseGame() {

        // Player
        total = 0;
        playerMoney = 0;
        player.setPosition(200, 200);
        player.setVelocity(0, 0);
        player.show();
        facingRight = true;
        if (currentLevel == 1 && !levelCompleted) { // Sets the players lives
            playerLives = 3;
        }
        isInvincible = false; // Cooldown for damage taken
        dead = false;

        // Bullets
        bullets.clear();

        // Money
        moneyDrops.clear();
        moneyVelocities.clear();
        moneyDropTimes.clear();

        // Zombie
        playingDeathAnimation = false;
        dyingZombies.clear();
        zombieDeathTimes.clear();
        dyingZombieTypes.clear();

        zombies.clear();
        zombieTypes.clear();
        zombieAlerted.clear();

        // Game
        levelCompleted = false;
        gameEnd = false;
        if (currentLevelMusicClip != null) {
            fadeOutMusic(currentLevelMusicClip);
        }

        // Load zombies for level 1
        if (currentLevel == 1) {
            currentLevelMusicClip = level1MusicClip;
            fadeInMusic(currentLevelMusicClip);
            createZombie(1200, 200);
            createZombie(500, 200);
            createZombie(800, 200);
            createZombie(1990, 600);
            createZombie(3650, 650);
            createZombie(2550, 500);
        }

        else if (currentLevel == 2) {
            currentLevelMusicClip = level2MusicClip;
            fadeInMusic(currentLevelMusicClip);
            createZombie(1200, 200);
            createZombie(500, 200);
            createZombie(800, 200);
            createZombie(1990, 600);
            createZombie(3650, 650);
            createZombie(2550, 500);
            createZombie(1730, 650);
            createZombie(3700, 650);
            createZombie(3147, 100);
        }

    }

    // --------------------------------------------------------------------------------------------
    // Draw
    // --------------------------------------------------------------------------------------------

    /**
     * Draw the current state of the game. Note the sample use of
     * debugging output that is drawn directly to the game screen.
     */
    public void draw(Graphics2D g) {

        // Be careful about the order in which you draw objects - you
        // should draw the background first, then work your way 'forward'

        // First work out how much we need to shift the view in order to
        // see where the player is. To do this, we adjust the offset so that
        // it is relative to the player's position along with a shift

        StartScreen startScreen = new StartScreen();
        EndScreen endScreen = new EndScreen();

        if (!gameStarted) {
            startScreen.render(g, getWidth(), getHeight());
        } else if (gameEnd) {
            endScreen.EndScreenGraphics(g, getWidth(), getHeight());

            // Play the Game Over sound and wait until it's finished
            if (gameOverSoundClip != null) {
                gameOverSoundClip.setFramePosition(0); // Reset to start
                gameOverSoundClip.start(); // Play the clip

                // Wait until the clip finishes playing before proceeding
                while (gameOverSoundClip.isRunning()) {
                    try {
                        Thread.sleep(100); // Wait until the clip finishes playing
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }

        } else {
            int xo = -(int) player.getX() + 500;
            int yo = -(int) player.getY() + 400;

            g.setColor(Color.white);
            g.fillRect(0, 0, getWidth(), getHeight());

            parallaxBg.setCameraOffset(-player.getX() + 500, -player.getY() + 400);
            parallaxBg.draw(g);

            // Apply offsets to sprites then draw them
            for (Sprite s : clouds) {
                s.setOffsets(xo, yo);
                s.draw(g);
            }

            // Apply offsets to tile map and draw it
            tmap.draw(g, xo, yo);

            // Apply offsets to bullet and draw it
            for (Sprite bullet : bullets) {
                bullet.setOffsets(xo, yo - 20);
                bullet.draw(g);
            }

            // Apply offsets to money and draw it
            for (Sprite money : moneyDrops) {
                money.setOffsets(xo, yo);
                money.draw(g);
            }

            // Apply offsets to zombie and draw it
            for (Sprite zombie : zombies) {
                zombie.setOffsets(xo, yo);
                zombie.draw(g);
            }

            // Apply offsets to dying zombie and draw it
            for (Sprite dyingZombie : dyingZombies) {
                dyingZombie.setOffsets(xo, yo);
                dyingZombie.draw(g);
            }

            // Apply offsets to player and draw
            player.setOffsets(xo, yo);

            // Apply palyer invincibility
            if (isInvincible && !playingDeathAnimation) {
                long currentTime = System.currentTimeMillis();
                if ((currentTime / 200) % 2 == 0) {
                    player.draw(g);
                }
            } else {
                player.draw(g);
            }

            // Show score, lives and money information
            String msg = String.format("Score: %d", total / 100);
            g.setFont(new Font("Arial", Font.BOLD, 15));
            g.setColor(Color.WHITE);
            g.drawString(msg, getWidth() - 150, 50);

            String moneyMsg = String.format("Money: $%d", playerMoney);
            g.setColor(Color.white);
            g.drawString(moneyMsg, getWidth() - 150, 70);

            // Displying the player's current lives
            drawLives(g);

            // Displaying the tiny increment counter
            if (showMoneyCounter) {
                g.setColor(Color.green);
                g.setFont(new Font("Arial", Font.BOLD, 16));
                String counterText = "+$1";
                g.drawString(counterText, xo + moneyCounterX, yo + moneyCounterY);
            }

            // Dispying the level message
            if (showingLevelMessage) {
                g.setColor(new Color(0, 0, 0, 180));
                g.fillRect(0, 150, getWidth(), 60);
                g.setColor(Color.white);
                g.setFont(new Font("Arial", Font.BOLD, 24));
                String levelMsg = "Level " + currentLevel;
                int msgWidth = g.getFontMetrics().stringWidth(levelMsg);
                g.drawString(levelMsg, (getWidth() - msgWidth) / 2, 190);
            }

            // Displaying level completed message
            if (levelCompleted) {
                g.setColor(new Color(0, 0, 0, 180));
                g.fillRect(0, 150, getWidth(), 100);
                g.setColor(Color.white);
                g.setFont(new Font("Arial", Font.BOLD, 24));
                String levelMsg = "Level " + currentLevel + " Complete!";
                int msgWidth = g.getFontMetrics().stringWidth(levelMsg);
                g.drawString(levelMsg, (getWidth() - msgWidth) / 2, 190);

                g.setFont(new Font("Arial", Font.PLAIN, 16));
                String goodJobMsg = "Good Job !";
                int goodJobWidth = g.getFontMetrics().stringWidth(goodJobMsg);
                g.drawString(goodJobMsg, (getWidth() - goodJobWidth) / 2, 220);
            }

            // Verifiying if debub mode is on or off
            if (debug) {

                // When in debug mode, you could draw borders around objects
                // and write messages to the screen with useful information.
                // Try to avoid printing to the console since it will produce
                // a lot of output and slow down your game.

                tmap.drawBorder(g, xo, yo, Color.black);
                g.setColor(Color.red);
                player.drawBoundingBox(g);
                g.drawString(String.format("Player: %.0f,%.0f", player.getX(), player.getY()),
                        getWidth() - 150, 90);
                g.drawString(String.format("Bullets: %d", bullets.size()),
                        getWidth() - 150, 110);
                g.drawString(String.format("Lives: %d", playerLives),
                        getWidth() - 150, 130);
                g.drawString(String.format("Zombies: %d", zombies.size()),
                        getWidth() - 150, 150);
                g.drawString(String.format("Level: %d", currentLevel),
                        getWidth() - 150, 170);
                g.drawString(String.format("Money: %d", playerMoney),
                        getWidth() - 150, 190);
                drawCollidedTiles(g, tmap, xo, yo);

                g.setColor(Color.green);
                for (Sprite bullet : bullets) {
                    bullet.drawBoundingBox(g);
                }

                g.setColor(Color.orange);
                for (Sprite zombie : zombies) {
                    zombie.drawBoundingBox(g);
                }

                g.setColor(Color.green);
                for (Sprite money : moneyDrops) {
                    money.drawBoundingBox(g);
                }
            }
        }
    }

    private void drawLives(Graphics2D g) {
        int heartWidth = 32;
        int padding = 5;

        for (int i = 0; i < playerLives; i++) {

            // Create new heart sprite
            Sprite heart = new Sprite(heartAnim);

            // Set heart sprite position
            heart.setPosition(20 + i * (heartWidth + padding), 50);
            heart.setOffsets(0, 0);

            // Draw heart image to the screen
            heart.draw(g);
        }
    }

    public void drawCollidedTiles(Graphics2D g, TileMap map, int xOffset, int yOffset) {
        if (collidedTiles.size() > 0) {

            // Find out how wide and how tall a tile is
            int tileWidth = map.getTileWidth();
            int tileHeight = map.getTileHeight();

            g.setColor(Color.blue);
            for (Tile t : collidedTiles) {
                g.drawRect(t.getXC() + xOffset, t.getYC() + yOffset, tileWidth, tileHeight);
            }
        }
    }

    // --------------------------------------------------------------------------------------------
    // Element Updates / Element Creation
    // --------------------------------------------------------------------------------------------

    /**
     * Update any sprites and other game elements, also check for collisions
     * 
     * @param elapsed The elapsed time between this call and the previous call of
     *                elapsed
     */
    public void update(long elapsed) {
        super.update(elapsed);

        // Update the parallax background with the player's X position
        parallaxBg.update(player.getX());

        // Dispaly the current level at the start of the game
        if (showingLevelMessage) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - levelMessageStartTime > levelMessageDuration) {
                showingLevelMessage = false;
            }
            return;
        }

        // Update the money counter
        if (showMoneyCounter) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - moneyCounterStartTime > moneyCounterDuration) {
                showMoneyCounter = false;
            }
        }

        // Check if current level is comepleted
        if (levelCompleted) {
            long currentTime = System.currentTimeMillis();
            if (!isFadingOut && currentLevelMusicClip != null) {
                fadeOutMusic(currentLevelMusicClip); // Fade out music to level background audio.
            }

            if (currentTime - levelCompletedTime > levelCompletedDuration) {
                levelCompleted = false;
                if (currentLevel == 1) {
                    int savedLives = playerLives; // Player lives carry over to the next level
                    int savedMoney = playerMoney; // Player money carrys over to the next level
                    loadLevel(2);
                    initialiseGame();
                    playerLives = savedLives;
                    playerMoney = savedMoney;

                } else if (currentLevel == 2) {
                    if (currentLevelMusicClip != null) {
                        fadeOutMusic(currentLevelMusicClip);
                    }
                    gameEnd = true;
                    if (gameOverSoundClip != null) {
                        gameOverSoundClip.setFramePosition(0); // Reset to start
                        gameOverSoundClip.start(); // Play the clip

                        // Wait until the clip finishes
                        while (gameOverSoundClip.isRunning()) {
                            try {
                                Thread.sleep(100); // Wait until the clip finishes playing
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }
            return;
        }

        // Check if player is dead
        if (playerLives <= 0 && !playingDeathAnimation) {
            playingDeathAnimation = true;
            deathAnimationStartTime = System.currentTimeMillis();
            player.setAnimation(deathAnim); // Load the Player death animation
            ReverbAudioPlayer.playReverbAudio("sounds/player_death.wav"); // Play the pleaer death music with reverb
            player.getAnimation();
            player.getAnimation().start(); // Player the playe death animation
            player.setVelocityX(0);
            player.setVelocityY(0);

            // Fade out the current backgorund music
            if (currentLevelMusicClip != null) {
                fadeOutMusic(currentLevelMusicClip);
            }
            return;
        }

        // Condition to dispaly the game over screen
        if (playingDeathAnimation) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - deathAnimationStartTime > deathAnimationDuration) {
                gameEnd = true; // Toggle game over to display the game over screen

                // Stop any background music
                if (currentLevelMusicClip != null) {
                    fadeOutMusic(currentLevelMusicClip);
                }
            }
            return;
        }

        // Udpate player invincibility effect after cooldown
        if (isInvincible) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - invincibilityTime > invincibilityDuration) {
                isInvincible = false; // Toggel player invincibility off
            }
        }

        // Update level completed staus if all the zombies are dead
        if (zombies.isEmpty() && dyingZombies.isEmpty() && !levelCompleted) { // Verify if all the zombies are killed
            levelCompleted = true;
            levelCompletedTime = System.currentTimeMillis();
            return;
        }

        float velocityX = 0;

        // Update the player sprite if shoot is pressed baised on the current direction
        if (shoot) {
            Animation shootAnim = facingRight ? shootRight : shootLeft; // Check direction of palyer
            if (player.getAnimation() != shootAnim) {
                player.setAnimation(shootAnim);
                player.getAnimation().start();
            }

            long currentTime = System.currentTimeMillis(); // Shoot in the current player direction
            if (currentTime - lastShotTime > shootCooldown) {
                createBullet();
                lastShotTime = currentTime;
            }
        }

        // Update player movement and direction for left side
        else if (moveLeft) {
            velocityX = -moveSpeed;
            if (player.getAnimation() != walkLeft) { // Toggles palyer walk left animation
                player.setAnimation(walkLeft);
                player.getAnimation().start();
            }
            facingRight = false; // Updates player facing side
        }

        // Updates player movement and direction for right side
        else if (moveRight) {
            velocityX = moveSpeed;
            if (player.getAnimation() != walkRight) { // Toggles palyer walk left animation
                player.setAnimation(walkRight);
                player.getAnimation().start();
            }
            facingRight = true; // Updates player facing side
        }

        // Update players idle animaiton on the direction it was facing
        else {
            Animation idleAnim = facingRight ? idleRight : idleLeft;
            if (player.getAnimation() != idleAnim) { // Toggleing animation baised on direction
                player.setAnimation(idleAnim);
                player.getAnimation().start();
            }
        }

        player.setVelocityX(velocityX * elapsed);

        float velocityY = player.getVelocityY();

        // Updating palyer velocity while jumping
        if (jumping && isOnGround) {
            velocityY = jumpVelocity;
            isOnGround = false;
        }

        velocityY += gravity * elapsed;

        if (velocityY > maxFallSpeed) {
            velocityY = maxFallSpeed;
        }

        player.setVelocityY(velocityY);

        player.update(elapsed);

        updateBullets(elapsed);

        updateZombies(elapsed);

        updateDyingZombies(elapsed);

        updateMoneyDrops(elapsed);

        checkTileCollision(player, tmap);

        checkZombiePlayerCollisions();

        checkZombieBulletCollisions();

        checkMoneyPlayerCollisions();

        if (player.getY() > tmap.getPixelHeight()) {
            loseLife();
            player.setPosition(200, 200);
        }

        // Out of bounds collision handeling
        handleScreenEdge(player, tmap, elapsed);

        // Updating the score
        total += elapsed;
    }

    private void updateMoneyDrops(long elapsed) {
        for (int i = 0; i < moneyDrops.size(); i++) {
            Sprite money = moneyDrops.get(i);
            float velocity = moneyVelocities.get(i);
            long dropTime = moneyDropTimes.get(i);

            // Apply gravity to the falling money
            velocity += gravity * elapsed;
            if (velocity > maxFallSpeed) {
                velocity = maxFallSpeed; // Limit falling speed
            }

            money.setVelocityY(velocity);
            money.update(elapsed); // Update the position based on velocity

            checkTileCollision(money, tmap); // Prevent money from passing through tiles
            handleScreenEdge(money, tmap, elapsed); // Handle behavior at screen edges

            moneyVelocities.set(i, velocity); // Update velocity in the list

            // Remove money if it has exceeded its lifespan
            long currentTime = System.currentTimeMillis();
            if (currentTime - dropTime > moneyLifespan) {
                moneyDrops.remove(i);
                moneyVelocities.remove(i);
                moneyDropTimes.remove(i);
                i--; // Adjust index after removal
            }
        }
    }

    private void updateDyingZombies(long elapsed) {
        // Use iterators to iterate through zombies, their death times, and their types
        Iterator<Sprite> zombieIterator = dyingZombies.iterator();
        Iterator<Long> timeIterator = zombieDeathTimes.iterator();
        Iterator<Integer> typeIterator = dyingZombieTypes.iterator();

        while (zombieIterator.hasNext() && timeIterator.hasNext() && typeIterator.hasNext()) {
            Sprite zombie = zombieIterator.next();
            Long deathTime = timeIterator.next();
            Integer zombieType = typeIterator.next();

            zombie.update(elapsed); // Update zombie animation/movement

            checkTileCollision(zombie, tmap); // Prevent zombies from clipping through tiles
            handleScreenEdge(zombie, tmap, elapsed); // Handle screen boundaries

            long currentTime = System.currentTimeMillis();

            // Remove the zombie if it has been "dying" for more than 1.5 seconds
            if (currentTime - deathTime > 1500) {
                zombieIterator.remove();
                timeIterator.remove();
                typeIterator.remove();
            }
        }
    }

    private void loseLife() {
        if (!isInvincible) {
            playerLives--;
            isInvincible = true;
            invincibilityTime = System.currentTimeMillis();
        }
    }

    private void updateZombies(long elapsed) {
        for (int i = 0; i < zombies.size(); i++) {
            Sprite zombie = zombies.get(i);
            int zombieType = zombieTypes.get(i);
            boolean alerted = zombieAlerted.get(i);

            // Apply gravity to the zombie's vertical velocity
            float zombieVelocityY = zombie.getVelocityY();
            zombieVelocityY += gravity * elapsed;
            if (zombieVelocityY > maxFallSpeed) {
                zombieVelocityY = maxFallSpeed; // Limit fall speed
            }
            zombie.setVelocityY(zombieVelocityY);

            // Determine the zombie's distance from the player
            float distanceToPlayer = Math.abs(zombie.getX() - player.getX());

            if (distanceToPlayer < zombieDetectionRange) {
                // If not already alerted, play a zombie sound
                if (!alerted) {
                    ReverbAudioPlayer.playReverbAudio("sounds/zombie.wav");
                    zombieAlerted.set(i, true);
                }

                // Move towards the player
                boolean zombieFacingRight = player.getX() > zombie.getX();
                float zombieVelocityX = zombieFacingRight ? zombieSpeed * elapsed : -zombieSpeed * elapsed;
                zombie.setVelocityX(zombieVelocityX);

                // Set walking animation if not already set
                Animation zombieWalkAnim = zombieFacingRight ? zombieWalkRight[zombieType] : zombieWalkLeft[zombieType];
                if (zombie.getAnimation() != zombieWalkAnim) {
                    zombie.setAnimation(zombieWalkAnim);
                    zombie.getAnimation().start();
                }
            } else {
                // Stop movement and reset alert status
                zombie.setVelocityX(0);
                zombieAlerted.set(i, false);

                // Set idle animation based on direction
                boolean zombieFacingRight = zombie.getVelocityX() >= 0;
                Animation zombieIdleAnim = zombieFacingRight ? zombieIdleRight[zombieType] : zombieIdleLeft[zombieType];
                if (zombie.getAnimation() != zombieIdleAnim) {
                    zombie.setAnimation(zombieIdleAnim);
                    zombie.getAnimation().start();
                }
            }

            zombie.update(elapsed); // Update zombie position and animation
            checkTileCollision(zombie, tmap); // Handle tile-based collisions
        }
    }

    private void createBullet() {
        new Sound("sounds/shoot.wav").start(); // Play shooting sound

        Sprite bullet = new Sprite(bulletAnim); // Create a new bullet sprite

        float bulletX, bulletY;

        // Set bullet's initial position based on player direction
        if (facingRight) {
            bulletX = player.getX() + player.getWidth() - 10;
        } else {
            bulletX = player.getX() - bullet.getWidth() + 10;
        }

        bulletY = player.getY() + player.getHeight() / 2 - bullet.getHeight() / 2;
        bullet.setPosition(bulletX, bulletY);

        // Set bullet speed based on player's direction
        float bulletVelX = facingRight ? bulletSpeed : -bulletSpeed;
        bullet.setVelocity(bulletVelX, 0);

        bullets.add(bullet); // Add bullet to the active bullets list
    }

    private void createZombie(float x, float y) {
        int zombieType = random.nextInt(4); // Randomly select a zombie type
        Sprite zombie = new Sprite(zombieIdleRight[zombieType]); // Assign the idle animation
        zombie.setPosition(x, y);
        zombie.setVelocity(0, 0);

        // Store zombie data in their respective lists
        zombies.add(zombie);
        zombieTypes.add(zombieType);
        zombieAlerted.add(false);
    }

    private void createMoneyDrop(float x, float y) {
        Sprite money = new Sprite(moneyAnim); // Create a money sprite
        money.setPosition(x, y);

        // Assign initial velocity (jump effect when spawned)
        money.setVelocityY(moneyJumpVelocity);
        money.setVelocityX((random.nextFloat() - 0.5f) * 0.2f);

        // Store money properties for tracking
        moneyDrops.add(money);
        moneyVelocities.add(money.getVelocityY());
        moneyDropTimes.add(System.currentTimeMillis());
    }

    private void updateBullets(long elapsed) {
        Iterator<Sprite> it = bullets.iterator();

        while (it.hasNext()) {
            Sprite bullet = it.next();
            bullet.update(elapsed); // Move the bullet

            // Check if the bullet collides with tiles
            if (checkBulletTileCollision(bullet, tmap)) {
                it.remove();
                continue;
            }

            // Remove the bullet if it goes out of the screen
            if (bullet.getX() < 0 || bullet.getX() > tmap.getPixelWidth() ||
                    bullet.getY() < 0 || bullet.getY() > tmap.getPixelHeight()) {
                it.remove();
            }
        }
    }

    // --------------------------------------------------------------------------------------------
    // Collisions
    // --------------------------------------------------------------------------------------------

    private void checkZombiePlayerCollisions() {
        if (isInvincible)
            return;

        for (Sprite zombie : zombies) {
            // Check for collision with the player
            if (player.getX() < zombie.getX() + zombie.getWidth() &&
                    player.getX() + player.getWidth() > zombie.getX() &&
                    player.getY() < zombie.getY() + zombie.getHeight() &&
                    player.getY() + player.getHeight() > zombie.getY()) {

                loseLife();

                // Apply knockback effect to the player
                float knockbackX = player.getX() > zombie.getX() ? 0.3f : -0.3f;
                player.setVelocityX(knockbackX);
                player.setVelocityY(jumpVelocity);
                break;
            }
        }
    }

    private void checkZombieBulletCollisions() {
        Iterator<Sprite> bulletIterator = bullets.iterator();

        while (bulletIterator.hasNext()) {
            Sprite bullet = bulletIterator.next();
            Iterator<Sprite> zombieIterator = zombies.iterator();
            Iterator<Integer> zombieTypeIterator = zombieTypes.iterator();
            Iterator<Boolean> zombieAlertIterator = zombieAlerted.iterator();

            while (zombieIterator.hasNext()) {
                Sprite zombie = zombieIterator.next();
                int zombieType = zombieTypeIterator.next();
                zombieAlertIterator.next(); // Move iterator forward

                // Check if bullet collides with a zombie
                if (bullet.getX() < zombie.getX() + zombie.getWidth() &&
                        bullet.getX() + bullet.getWidth() > zombie.getX() &&
                        bullet.getY() < zombie.getY() + zombie.getHeight() &&
                        bullet.getY() + bullet.getHeight() > zombie.getY()) {

                    // Replace zombie with its dying animation
                    Sprite dyingZombie = new Sprite(zombieDeath[zombieType]);
                    dyingZombie.setPosition(zombie.getX(), zombie.getY());
                    dyingZombie.getAnimation().start();

                    // Play zombie death sound
                    playAudioFile("sounds/zombie_death.wav");

                    dyingZombies.add(dyingZombie);
                    zombieDeathTimes.add(System.currentTimeMillis());
                    dyingZombieTypes.add(zombieType);

                    // Remove zombie and bullet from the game
                    zombieIterator.remove();
                    zombieTypeIterator.remove();
                    zombieAlertIterator.remove();
                    bulletIterator.remove();

                    createMoneyDrop(zombie.getX() + zombie.getWidth() / 2, zombie.getY() + zombie.getHeight() / 2);
                    total += 1000;
                    break;
                }
            }
        }
    }

    private void checkMoneyPlayerCollisions() {
        for (int i = 0; i < moneyDrops.size(); i++) {
            Sprite money = moneyDrops.get(i);

            // Check if the player collects money
            if (player.getX() < money.getX() + money.getWidth() &&
                    player.getX() + player.getWidth() > money.getX() &&
                    player.getY() < money.getY() + money.getHeight() &&
                    player.getY() + player.getHeight() > money.getY()) {

                playerMoney++;
                showMoneyCounter = true;
                moneyCounterValue = playerMoney;
                moneyCounterStartTime = System.currentTimeMillis();
                moneyCounterX = player.getX();
                moneyCounterY = player.getY() - 20;

                total += 500;

                // Remove collected money
                moneyDrops.remove(i);
                moneyVelocities.remove(i);
                moneyDropTimes.remove(i);
                i--;

                ReverbAudioPlayer.playReverbAudio("sounds/money.wav");
            }
        }
    }

    private boolean checkBulletTileCollision(Sprite bullet, TileMap tmap) {

        // Getting bullet details
        float bx = bullet.getX();
        float by = bullet.getY();
        float bw = bullet.getWidth();
        float bh = bullet.getHeight();

        int startX = (int) (bx / tmap.getTileWidth());
        int startY = (int) (by / tmap.getTileHeight());
        int endX = (int) ((bx + bw - 1) / tmap.getTileWidth());
        int endY = (int) ((by + bh - 1) / tmap.getTileHeight());

        // Tiles that are solid taken from tile map
        Set<Character> solidTiles = Set.of('m', 'l', 'r', 'i', 'o', 't', 'd', 'y', 'f');

        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                if (!tmap.valid(x, y))
                    continue;

                char tileChar = tmap.getTileChar(x, y);
                if (solidTiles.contains(tileChar)) {
                    // Check if bullet intersects with a solid tile
                    float tileX = x * tmap.getTileWidth();
                    float tileY = y * tmap.getTileHeight();

                    if (bx < tileX + tmap.getTileWidth() && bx + bw > tileX &&
                            by < tileY + tmap.getTileHeight() && by + bh > tileY) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Check and handles collisions with a tile map for the
     * given sprite 's'. Initial functionality is limited...
     * 
     * @param s    The Sprite to check collisions for
     * @param tmap The tile map to check
     */

    public void checkTileCollision(Sprite s, TileMap tmap) {

        // Empty out our current set of collided tiles
        collidedTiles.clear();
        isOnGround = false;

        // Take a note of a sprite's current position
        float sx = s.getX();
        float sy = s.getY();
        float sw = s.getWidth();
        float sh = s.getHeight();

        // Take note of the tiles current position
        int startX = (int) (sx / tmap.getTileWidth());
        int startY = (int) (sy / tmap.getTileHeight());
        int endX = (int) ((sx + sw - 1) / tmap.getTileWidth());
        int endY = (int) ((sy + sh - 1) / tmap.getTileHeight());

        boolean collisionX = false;
        boolean collisionY = false;

        boolean hitDamagingTile = checkDamagingTileCollision(s, tmap, startX, startY, endX, endY);

        // This stores the tiles that are supposed to be made solid
        Set<Character> solidTiles = Set.of('m', 'l', 'r', 'i', 'o', 't', 'd', 'y', 'f');

        for (int y = startY; y <= endY; y++) {

            for (int x = startX; x <= endX; x++) {
                
                if (!tmap.valid(x, y))
                    continue;

                char tileChar = tmap.getTileChar(x, y);
                if (solidTiles.contains(tileChar)) {
                    float tileX = x * tmap.getTileWidth();
                    float tileY = y * tmap.getTileHeight();

                    // Calculate the overlapping values of co-ordinage X and Y
                    float overlapX = Math.min(sx + sw, tileX + tmap.getTileWidth()) - Math.max(sx, tileX);
                    float overlapY = Math.min(sy + sh, tileY + tmap.getTileHeight()) - Math.max(sy, tileY);

                    // Resolve collision based on overlap direction
                    if (overlapX > 0 && overlapY > 0) {
                        if (overlapX < overlapY) {
                            s.setX(sx < tileX ? tileX - sw : tileX + tmap.getTileWidth());
                            s.setVelocityX(0);
                            collisionX = true;
                        } else {
                            s.setY(sy < tileY ? tileY - sh : tileY + tmap.getTileHeight());
                            if (s.getVelocityY() > 0 && s == player) {
                                isOnGround = true;
                            }
                            s.setVelocityY(0);
                            collisionY = true;
                        }
                    }
                }
            }
        }
    }

    private boolean checkDamagingTileCollision(Sprite s, TileMap tmap, int startX, int startY, int endX, int endY) {
        if (s != player)
            return false;

        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                if (!tmap.valid(x, y))
                    continue;

                // Check if player cordinagtes match with damaging tile
                if (tmap.getTileChar(x, y) == 'j') {

                    // Player takes damage from harmful tile
                    if (!isInvincible) {
                        loseLife();
                        s.setVelocityY(jumpVelocity / 2);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks and handles collisions with the edge of the screen. You should
     * generally
     * use tile map collisions to prevent the player leaving the game area. This
     * method
     * is only included as a temporary measure until you have properly developed
     * your
     * tile maps.
     * 
     * @param s       The Sprite to check collisions for
     * @param tmap    The tile map to check
     * @param elapsed How much time has gone by since the last call
     */
    public void handleScreenEdge(Sprite s, TileMap tmap, long elapsed) {

        // This method just checks if the sprite has gone off the bottom screen.
        // Ideally you should use tile collision instead of this approach

        float difference = s.getY() + s.getHeight() - tmap.getPixelHeight();
        if (difference > 0) {
            // Put the player back on the map according to how far over they were
            s.setY(tmap.getPixelHeight() - s.getHeight() - (int) (difference));

            // and make them bounce
            s.setVelocityY(-s.getVelocityY() * 0f);

            if (s == player) {
                isOnGround = true;
            }
        }
    }

    /**
     * Use the sample code in the lecture notes to properly detect
     * a bounding box collision between sprites s1 and s2.
     * 
     * @return true if a collision may have occurred, false if it has not.
     */
    public boolean boundingBoxCollision(Sprite s1, Sprite s2) {
        return false;
    }

    // --------------------------------------------------------------------------------------------
    // Key Events / Mouse Events
    // --------------------------------------------------------------------------------------------

    /**
     * Override of the keyPressed event defined in GameCore to catch our
     * own events
     * 
     * @param e The event that has been generated
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // Start screen key events
        if (!gameStarted) {
            if (key == KeyEvent.VK_ENTER) {
                gameStarted = true;
                initialiseGame();
            } else if (key == KeyEvent.VK_ESCAPE) {
                stop();
            }
        }

        else if (gameEnd) {

            // End screen key events
            if (key == KeyEvent.VK_ENTER) {
                gameStarted = true;
                gameEnd = false;
                initialiseGame();
            } else if (key == KeyEvent.VK_ESCAPE) {
                stop();
            }
        }

        // During game key events
        else {

            // Player actions start key events
            switch (key) {
                case KeyEvent.VK_SPACE:
                case KeyEvent.VK_UP:
                    jumping = true; // Make the player jump
                    break;
                case KeyEvent.VK_LEFT:
                    moveLeft = true; // Move the player to the left
                    break;
                case KeyEvent.VK_RIGHT:
                    moveRight = true; // Move the player to the right
                    break;
                case KeyEvent.VK_S:
                    shoot = true; // Make the palyer shoot
                    break;
                case KeyEvent.VK_ESCAPE:
                    stop(); // Close the game
                    break;
                case KeyEvent.VK_R:
                    initialiseGame(); // Restart the game
                    break;
                case KeyEvent.VK_B:
                    debug = !debug; // Toggle debug mode
                    break;
            }
        }
    }

    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        // Player actions end key events
        switch (key) {
            case KeyEvent.VK_SPACE:
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                jumping = false; // Toggle jumping boolean off
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                moveLeft = false; // Toggle left charecter movement off
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                moveRight = false; // Toggle right charecter movement off
                break;
            case KeyEvent.VK_S:
                shoot = false; // Toogle shoot for charecter off
                break;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // This is called after a press AND release.
        if (gameStarted && !gameEnd) {
            // Toggle debug mode on
            if (e.getButton() == MouseEvent.BUTTON1) {
                debug = !debug;
            }
        }
    }

    // Shoot when right click is pressed
    @Override
    public void mousePressed(MouseEvent e) {

        // Only shoot if game is active
        if (gameStarted && !gameEnd && !playingDeathAnimation) {
            if (e.getButton() == MouseEvent.BUTTON3) {
                shoot = true;
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Stop shooting when the RIGHT mouse button is released
        if (e.getButton() == MouseEvent.BUTTON3) {
            shoot = false;
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) { // Only stop shooting if it was the right button
            shoot = false;
        }
    }

    // -------------------------------------------------------------------------------------------

}