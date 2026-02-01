package pepse.world;

import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import pepse.utils.ColorSupplier;
import pepse.utils.NoiseGenerator;
import pepse.world.Block;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * responsible for generating the terrain of the world.
 * The terrain height is based on a deterministic noise function, so the same x value always
 * produces the same ground height for a given seed
 *
 * The terrain is created from {@link Block} objects
 */
public class Terrain {

    /**
     * The initial ground height is set to 2/3 of the window height.
     */
    public static final float BASE_GROUND_Y_FACTOR = 2f / 3f;

    /**
     * scales the noise input - how “smooth” or “wavy” the terrain look
     */
    private static final float NOISE_FACTOR = 10f;

    /**
     * The base color of the ground blocks
     */
    private static final Color BASE_GROUND_COLOR = new Color(212, 123, 74);

    /**
     * how many blocks are generated below the ground surface.
     */
    private static final int TERRAIN_DEPTH = 20;

    private final Vector2 windowDimensions;
    private final float groundHeightAtX0;
    private final NoiseGenerator noiseGenerator;

    /**
     * Creates a new terrain generator
     *
     * @param windowDimensions the window dimensions in pixels.
     * @param seed used for deterministic terrain generation
     */
    public Terrain(Vector2 windowDimensions, int seed) {
        this.windowDimensions = windowDimensions;
        this.groundHeightAtX0 = windowDimensions.y() * BASE_GROUND_Y_FACTOR;
        this.noiseGenerator = new NoiseGenerator(seed, (int) groundHeightAtX0);
    }

    /**
     * Computes the ground height at a given x coordinate
     *
     * @param x the x coordinate in world space.
     * @return The y coordinate of the ground surface at x
     */
    public float groundHeightAt(float x) {
        double noise = noiseGenerator.noise(x, Block.SIZE * NOISE_FACTOR);
        return (float) (groundHeightAtX0 + noise);
    }

    /**
     * creates all terrain blocks in the range {@code [minX, maxX]}
     *
     * @param minX The minimum x coordinate.
     * @param maxX The maximum x coordinate.
     * @return a list of {@link Block} objects that form the terrain in this range
     */
    public List<Block> createInRange(int minX, int maxX) {
        List<Block> blocks = new ArrayList<>();
        int size = Block.SIZE;

        int startX = (int) Math.floor((double) minX / size) * size;
        int endX = (int) Math.ceil((double) maxX / size) * size;

        for (int x = startX; x <= endX; x += size) {
            int topY = (int) Math.floor(groundHeightAt(x) / size) * size;

            for (int i = 0; i < TERRAIN_DEPTH; i++) {
                RectangleRenderable renderable =
                        new RectangleRenderable(ColorSupplier.approximateColor(BASE_GROUND_COLOR));
                Block block = new Block(new Vector2(x, topY + i * size), renderable);
                blocks.add(block);
            }
        }
        return blocks;
    }

}
