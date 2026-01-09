

## 📖 Project Overview
**Awakened** is a 2D side-scrolling platformer set in an apocalyptic world where the dead have risen. You play as a soldier tasked with eliminating the roaming zombies. 

To win, you must navigate through apocalyptic terrain, avoid poison bushes, and defeat enemies to collect cash. The game features two challenging levels. The player has **three lives** and a gun with unlimited ammunition. If all lives are lost, the game ends.

## 🎮 Game Controls

| Action | Keyboard Input | Mouse Input |
| :--- | :--- | :--- |
| **Jump** | `Up Arrow` or `Space` | - |
| **Move Left** | `Left Arrow` | - |
| **Move Right** | `Right Arrow` | - |
| **Shoot** | `S` | Right-Click |
| **Toggle Debug Mode** | `D` | Left-Click |
| **Restart Game** | `R` | - |
| **Close Game** | `Esc` | - |

## ✨ Features

### Core Mechanics
*   **Physics & Collision:** Custom collision detection for tile maps, bullets, and entities.
*   **Parallax Scrolling:** Background layers move at different speeds relative to the player to create depth.
*   **Combat System:** Shooting mechanics with unlimited ammo and zombie elimination.
*   **Lives System:** Player has 3 lives that carry over between levels.
*   **Invincibility Frames:** Player becomes temporarily invincible (visualized by flashing) after taking damage.

### User Interface & Experience
*   **Game Flow:** Dedicated Start Screen, Level Transition Indicators, and Game Over/Victory screens.
*   **Animations:** 
    *   *Player:* Idle, Walk, Shoot, Death.
    *   *Enemy:* Idle, Walk.
*   **Randomization:** Random zombie sprite generation upon game restart.
*   **Collectibles:** Zombies drop cash upon death with visual indicators and sound effects.

### Audio
*   **Sound Effects (WAV):** Shooting, jumping, collecting money, and damage sounds.
*   **Music (MIDI):** Unique background music for menus and game levels.
*   **Audio Filters:** Implementation of a Reverb filter on selected tracks for atmospheric depth.

### Developer Tools
*   **Debug Mode:** Pressing `D` or Left-Click visualizes bounding boxes and displays stats (Zombie count, Bullet count, Player coordinates).

## 🐛 Known Issues / Limitations
*   **Zombie Vertical Tracking:** Zombies may move continuously left-to-right when the player is directly above or below them.
*   **Stair Navigation:** Enemy AI occasionally gets stuck on stair steps.
*   **Animation Glitch:** Rarely, the zombie sprite may clip into the ground during its death animation.

## 🚀 Future Objectives
*   **Input Handling:** Improve user control configuration.
*   **Weaponry:** Add multiple weapon types and pickups.
*   **Content:** Add more levels, collectibles (chests), and a Boss Zombie.
*   **Character Selection:** Allow the player to choose different avatars.

## 📂 Installation & How to Run
1.  Clone the repository.
2.  Open the project in your preferred Java IDE (Eclipse, IntelliJ, NetBeans).
3.  Locate `Game.java` in the source folder.
4.  Run `Game.java` as the main class.

## 📚 References & Assets
*   **Language:** Java Swing Documentation
*   **Assets (CraftPix):** Exclusion Zone Tileset, Urban Zombie Sprite Sheet, Soldier Sprite Sheets, Nature Backgrounds.
*   **Audio:** MidiWorld, Pixabay.
