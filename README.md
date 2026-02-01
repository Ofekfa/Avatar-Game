# Pepse

Pepse is a small Java 2D game/demo that demonstrates procedural terrain and flora generation, a day/night cycle, a controllable avatar with energy mechanics, and dynamic chunk loading for large worlds. The project is built on top of a simple game engine (Danogl) and is intended for learning OOP and game systems.

## Features
- Procedural terrain generation (noise-based).
- Sky and day/night cycle with sun and halo.
- Procedural trees and flora (trunks, leaves, fruits).
- Chunked world loading/unloading to support large horizontal worlds.
- Controllable avatar with energy and a simple UI energy display.
- Fruit collection that increases avatar energy.

## Quick start

Requirements:
- JDK 11 or newer.
- Danogl engine JAR available on the classpath.
- IntelliJ IDEA (project tested with `IntelliJ IDEA 2025.2.3`).

To run:
1. Open the project in IntelliJ IDEA.
2. Make sure the Danogl dependency is configured on the project's run configuration/classpath.
3. Run the main class `src/pepse/PepseGameManager.java` (use the `main` method).

## Controls
- Keyboard controls are handled via the engine's `UserInputListener`. Typical controls: move left/right and jump (engine default keys).

## Project structure
- `src/pepse/PepseGameManager.java` — main game manager; sets up world, camera, and chunk loading.
- `src/pepse/world/Terrain.java` — procedural terrain generator and block creation.
- `src/pepse/world/Sky.java` — background sky handling.
- `src/pepse/world/daynight/*` — day/night cycle, `Sun`, `Night`, and `SunHalo`.
- `src/pepse/world/trees/*` — `Flora`, `FloraPack`, `Tree`, `Trunk`, `Leaf`, `Fruit`.
- `src/pepse/world/avatar/Avatar.java` — player avatar and energy mechanics.
- `src/pepse/world/ui/EnergyDisplay.java` — UI element for displaying energy.
- `src/pepse/utils/*` — helpers such as `NoiseGenerator` and `ColorSupplier`.

## Configuration highlights
Configuration constants are defined in `PepseGameManager` (examples):
- `CYCLE_LENGTH` — length of day/night cycle.
- `SEED` — PRNG seed for reproducible terrain/flora.
- `CHUNK_WIDTH`, `LOAD_RADIUS_CHUNKS` — control chunk size and how far around the avatar chunks are loaded.



## License
- No license specified. Add a `LICENSE` file if you wish to set explicit terms.
