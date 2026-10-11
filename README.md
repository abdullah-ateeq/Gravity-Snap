# Gravity Snap 🌌

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg)](https://developer.android.com/jetpack/compose)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24-orange.svg)](https://developer.android.com/about/dashboards)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-37-blue.svg)](https://developer.android.com/about/versions/15)

> *One tap changes everything.*

**Gravity Snap** is a fast-paced, endless arcade gravity runner natively built for Android using **Jetpack Compose**. Players guide a fast-moving neon cube through a dynamic cybernetic corridor, reversing gravity with a single tap to switch between running on the floor and running on the ceiling while dodging obstacles.

---

## ✨ Features

- ⚡ **Instantaneous Gravity Inversion**: Tap anywhere on the screen to flip gravity between floor and ceiling with smooth physics transitions.
- 🎨 **100% Jetpack Compose UI**: Built with Compose Canvas, custom shader-like visual modifiers, glassmorphism UI containers, glowing neon buttons, and particle bursts.
- 🧩 **Procedural Obstacle Engine**:
  - **Static Blocks**: Floor and ceiling barriers demanding swift reaction time.
  - **Double Barriers**: Top and bottom hazards leaving tight center gap passages.
  - **Patrol Obstacles**: Vertical moving hazards testing precision timing.
  - **Laser / Spike Barriers**: High-speed hazard obstacles.
- 💥 **Particle System**: Dynamic spark trails, impact bursts, land ripples, and close-call flash effects.
- 🔊 **Immersive Audio & Haptic Feedback**: Integrated native SoundPool SFX (snaps, landings, close calls, game over) paired with precise tactile haptic pulses.
- 📊 **Detailed Player Stats & Persistence**:
  - High score and best streak tracking.
  - Real-time run statistics: gravity flips count, survival duration, dodged barriers, and close-call near misses.
  - Powered by **AndroidX DataStore Preferences**.
- ⚙️ **Customization & Accessibility**:
  - Themes: Cyberpunk Dark, Neon Blue, and Vaporwave Light modes.
  - Particle controls and reduced motion / visual effects mode.
- 🔒 **100% Offline & Private**: Zero advertisements, no account sign-in needed, zero data collection.

---

## 🎮 How to Play

1. **Tap Anywhere**: Tap the screen to flip gravity.
2. **Floor & Ceiling**: Run on the floor or ceiling depending on incoming hazards.
3. **Avoid Obstacles**: Dodging barriers increases your score and game speed.
4. **Close Calls**: Skim closely past obstacles to earn bonus score points and trigger special close-call notifications!

---

## 🏗️ Architecture & Tech Stack

Gravity Snap is designed around modern Android best practices and Clean Architecture / Unidirectional Data Flow (UDF) patterns:

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
- **Architecture**: MVVM with `StateFlow` and Coroutines
- **Game Engine**: Custom 60FPS physics loop leveraging Compose `withFrameNanos`
- **Data Persistence**: [AndroidX DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore)
- **Audio Engine**: Android native `SoundPool` & `VibratorManager`
- **Build System**: Gradle with Version Catalogs (`libs.versions.toml`)

---

## 📂 Project Structure

```text
com.example.gravitysnap
├── audio
│   ├── HapticManager.kt       # Tactile vibration and haptic feedback
│   └── SoundManager.kt        # SoundPool SFX management
├── data
│   └── PreferencesManager.kt  # DataStore for stats, scores, and preferences
├── game
│   ├── GameEngine.kt          # Main physics loop, collision detection, & score calculation
│   ├── GameState.kt           # Game state representation & statistics models
│   ├── Obstacle.kt            # Procedural obstacle types & hitboxes
│   ├── Particle.kt            # Particle trail & explosion effects
│   └── Player.kt              # Player physics, gravity flip states, & bounding boxes
├── ui
│   ├── components             # Glass cards, glowing buttons, corridor backgrounds
│   ├── navigation             # App navigation flow (Splash, Home, Game, Result, Settings, About)
│   ├── screens                # Jetpack Compose screen components
│   └── theme                  # Color palettes, typography, & custom styling
└── viewmodel
    ├── GameViewModel.kt       # Bridge between GameEngine and Compose UI
    └── SettingsViewModel.kt   # Preferences management ViewModel
```

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: Ladybug (2024.2.1) or newer / Studio 2026.x
- **JDK**: Java 11 or 17
- **Android SDK**: Compile SDK 37, Min SDK 24

### Build & Run

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/abdullah-ateeq/Gravity-Snap.git
   cd Gravity-Snap
   ```

2. **Run Unit Tests**:
   ```bash
   ./gradlew test
   ```

3. **Build Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```

4. **Install on Connected Device**:
   ```bash
   ./gradlew installDebug
   ```

---

## 🧪 Unit Tests

Gravity Snap includes comprehensive unit tests for game mechanics, physics, and collision safety:
- Player gravity flip state machine.
- Floor/ceiling landing detection.
- Inset hitbox calculations for fair collision boundaries.
- Double barrier safe gap spacing verification.
- Close-call detection zone margins.

Run tests using:
```bash
./gradlew test
```

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).
