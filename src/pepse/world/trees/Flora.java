package pepse.world.trees;

import danogl.GameObject;
import pepse.world.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Responsible for creating flora (trees and their components)
 *
 * Flora generation is deterministic per x coordinate based on a global world seed. For each
 * aligned block column in the requested range, this class decides (with a fixed probability)
 * whether to create a tree. Created trees contribute trunk, leaves and fruit {@link GameObject}s
 * which are returned together in a {@link FloraPack}.
 */
public class Flora {
    private static final float TREE_PROBABILITY = 0.1f;

    private final int worldSeed;
    private final Function<Float, Float> groundHeightAt;
    private final Consumer<Float> onFruitCollected;

    /**
     * Creates a new Flora generator.
     *
     * @param groundHeightAt Function mapping x to ground surface y. used so trees are placed on the ground
     * @param worldSeed deterministic seed to ensure the same flora distribution for the same world.
     * @param onFruitCollected Callback that will be called when a fruit created by this flora is collected.
     */
    public Flora(Function<Float, Float> groundHeightAt,
                 int worldSeed,
                 Consumer<Float> onFruitCollected) {
        this.groundHeightAt = groundHeightAt;
        this.worldSeed = worldSeed;
        this.onFruitCollected = onFruitCollected;
    }

    /**
     * Creates all flora objects in the inclusive horizontal range [minX, maxX].
     *
     * the supplied range is aligned to block boundaries (multiples of {@link Block#SIZE}). For each
     * aligned column this method uses a deterministic Random instance (dependent on x and the world seed)
     * to decide whether to create a tree.
     *
     * @param minX Minimum x coordinate of requested range in world coordinates.
     * @param maxX Maximum x coordinate of requested range in world coordinates.
     * @return a {@link FloraPack} containing the trunks, leaves and fruits created in the range.
     */
    public FloraPack createInRange(int minX, int maxX) {
        List<GameObject> trunks = new ArrayList<>();
        List<GameObject> leaves = new ArrayList<>();
        List<GameObject> fruits = new ArrayList<>();

        int startX = alignToBlock(minX);
        int endX = alignToBlock(maxX);

        for (int x = startX; x <= endX; x += Block.SIZE) {
            Random perXRandom = new Random(Objects.hash(x, worldSeed));

            if (perXRandom.nextFloat() < TREE_PROBABILITY) {
                Tree tree = Tree.createAt(x, groundHeightAt, perXRandom, onFruitCollected);
                trunks.add(tree.trunk());
                leaves.addAll(tree.leaves());
                fruits.addAll(tree.fruits());
            }
        }
        return new FloraPack(trunks, leaves, fruits);
    }

    private int alignToBlock(int x) {
        return (int) Math.floor((double) x / Block.SIZE) * Block.SIZE;
    }
}
