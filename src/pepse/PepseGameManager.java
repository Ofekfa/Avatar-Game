package pepse;

import danogl.GameManager;
import danogl.GameObject;
import danogl.collisions.Layer;
import danogl.gui.ImageReader;
import danogl.gui.SoundReader;
import danogl.gui.UserInputListener;
import danogl.gui.WindowController;
import danogl.gui.rendering.Camera;
import danogl.util.Vector2;
import pepse.world.Block;
import pepse.world.Sky;
import pepse.world.Terrain;
import pepse.world.avatar.Avatar;
import pepse.world.daynight.Night;
import pepse.world.daynight.Sun;
import pepse.world.trees.Flora;
import pepse.world.trees.FloraPack;
import pepse.world.ui.EnergyDisplay;

import java.util.*;

/**
 * The main game manager for the Pepse game
 *
 * This class extends the engine {@link GameManager} and is responsible for initializing the
 * world (sky, terrain, flora, day/night cycle, avatar, UI)
 */
public class PepseGameManager extends GameManager {

    private static final float CYCLE_LENGTH = 30f;
    private static final int SEED = 1234;

    private static final int CHUNK_BLOCKS = 10;
    private static final int CHUNK_WIDTH = CHUNK_BLOCKS * Block.SIZE;
    private static final int LOAD_RADIUS_CHUNKS = 3;
    private static final int UNLOAD_EXTRA_CHUNKS = 2;

    private Terrain terrain;
    private Flora flora;
    private Avatar avatar;
    private Vector2 windowDimensions;

    private final Map<Integer, List<LayeredObject>> loadedChunks = new HashMap<>();

    /**
     * helper pairing a {@link GameObject} with its layer
     */
    private static class LayeredObject {
        final GameObject obj;
        final int layer;

        LayeredObject(GameObject obj, int layer) {
            this.obj = obj;
            this.layer = layer;
        }
    }

    /**
     * Initialize the game, this method is called by the engine when the game starts.
     * It register the sky, terrain, flora, day/night objects, avatar, UI and camera.
     *
     * @param imageReader Reader for load images.
     * @param soundReader Reader for load sounds.
     * @param inputListener Input listener for user controls.
     * @param windowController controller providing window dimensions
     */
    @Override
    public void initializeGame(
            ImageReader imageReader,
            SoundReader soundReader, UserInputListener inputListener,
            WindowController windowController) {
        super.initializeGame(imageReader, soundReader, inputListener, windowController);
        windowDimensions = windowController.getWindowDimensions();

        GameObject sky = Sky.create(windowDimensions);
        gameObjects().addGameObject(sky, Layer.BACKGROUND);

        terrain = new Terrain(windowDimensions, SEED);
        flora = new Flora(terrain::groundHeightAt, SEED, this::onFruitCollected);

        GameObject night = Night.create(windowController.getWindowDimensions(), CYCLE_LENGTH);
        gameObjects().addGameObject(night, Layer.FOREGROUND);

        GameObject sun = Sun.create(windowController.getWindowDimensions(), CYCLE_LENGTH);
        gameObjects().addGameObject(sun, Layer.BACKGROUND);

        GameObject sunHalo = pepse.world.daynight.SunHalo.create(sun);
        gameObjects().addGameObject(sunHalo, Layer.BACKGROUND);

        float initialX = windowDimensions.x() * 0.5f;
        float groundY = terrain.groundHeightAt(initialX);
        groundY = (float) (Math.floor(groundY / Block.SIZE) * Block.SIZE);
        Vector2 initialAvatarLocation = new Vector2(initialX, groundY - Avatar.getAvatarSize());

        avatar = new Avatar(initialAvatarLocation, inputListener, imageReader);
        gameObjects().addGameObject(avatar, Layer.DEFAULT);

        EnergyDisplay energyDisplay = new EnergyDisplay();
        gameObjects().addGameObject(energyDisplay, Layer.FOREGROUND);

        avatar.addEnergyChangedListener(energyDisplay::updateEnergy);

        Vector2 cameraOffset = windowDimensions.mult(0.5f).subtract(initialAvatarLocation);
        setCamera(new Camera(
                avatar,
                cameraOffset,
                windowDimensions,
                windowDimensions
        ));

        ensureChunksAround(avatar.getCenter().x());

    }

    /**
     * Update called by the engine every frame. Ensures chunks around the avatar are loaded and
     * then delegates to the super.
     *
     * @param deltaTime Time elapsed (in seconds) since the last frame
     */
    @Override
    public void update(float deltaTime) {
        ensureChunksAround(avatar.getCenter().x());
        super.update(deltaTime);
    }

    /**
     * Ensures that all chunks within the configured load radius around the given center x are
     * loaded. Chunks outside an extended unload radius are removed.
     *
     * @param centerX X-coordinate (in pixels) around which to ensure chunks are present
     */
    private void ensureChunksAround(float centerX) {
        int centerChunk = Math.floorDiv((int) Math.floor(centerX), CHUNK_WIDTH);
        int minChunk = centerChunk - LOAD_RADIUS_CHUNKS;
        int maxChunk = centerChunk + LOAD_RADIUS_CHUNKS;

        for (int chunkId = minChunk; chunkId <= maxChunk; chunkId++) {
            if (!loadedChunks.containsKey(chunkId)) {
                loadChunk(chunkId);
            }
        }

        // unload far chunks
        int unloadMin = minChunk - UNLOAD_EXTRA_CHUNKS;
        int unloadMax = maxChunk + UNLOAD_EXTRA_CHUNKS;

        Iterator<Map.Entry<Integer, List<LayeredObject>>> it = loadedChunks.entrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            int chunkId = entry.getKey();
            if (chunkId < unloadMin || chunkId > unloadMax) {
                unloadChunk(entry.getValue());
                it.remove();
            }
        }
    }

    /**
     * Load a single chunk,  will create terrain blocks and flora objects for the
     * horizontal span of the chunk and add them to the appropriate layers.
     *
     * @param chunkId The integer id of the chunk to load
     */
    private void loadChunk(int chunkId) {
        int startX = chunkId * CHUNK_WIDTH;
        int endX = startX + CHUNK_WIDTH;

        List<LayeredObject> created = new ArrayList<>();

        for (Block block : terrain.createInRange(startX, endX)) {
            gameObjects().addGameObject(block, Layer.STATIC_OBJECTS);
            created.add(new LayeredObject(block, Layer.STATIC_OBJECTS));
        }

        FloraPack pack = flora.createInRange(startX, endX);

        for (GameObject trunk : pack.getTrunks()) {
            gameObjects().addGameObject(trunk, Layer.STATIC_OBJECTS);
            created.add(new LayeredObject(trunk, Layer.STATIC_OBJECTS));
        }
        for (GameObject leaf : pack.getLeaves()) {
            gameObjects().addGameObject(leaf, Layer.DEFAULT);
            created.add(new LayeredObject(leaf, Layer.DEFAULT));
        }
        for (GameObject fruit : pack.getFruits()) {
            gameObjects().addGameObject(fruit, Layer.DEFAULT);
            created.add(new LayeredObject(fruit, Layer.DEFAULT));
        }

        loadedChunks.put(chunkId, created);
    }

    /**
     * Removes all game objects in the provided list from their layers.
     *
     * @param objects list of {@link LayeredObject} to remove from the engine.
     */
    private void unloadChunk(List<LayeredObject> objects) {
        for (LayeredObject lo : objects) {
            gameObjects().removeGameObject(lo.obj, lo.layer);
        }
    }

    /**
     * Called by fruit when collected to apply the energy bonus to the avatar.
     *
     * @param energyBonus amount of energy to add to the avatar.
     */
    private void onFruitCollected(float energyBonus) {
        if (avatar != null) {
            avatar.increaseEnergy(energyBonus);
        }
    }


    /**
     * Main entry point used to start the game
     *
     * @param args command-line arguments.
     */
    public static void main(String[] args) {
        new PepseGameManager().run();
    }
}
