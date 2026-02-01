package pepse.world;

import danogl.GameObject;
import danogl.components.GameObjectPhysics;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

/**
 * A single terrain block in the world.
 * blocks are static - immovable and are used to build the ground
 */
public class Block extends GameObject {

    /**
     * The block is a square of SIZE x SIZE
     */
    public static final int SIZE = 30;

    /**
     * creates a new terrain block
     *
     * @param topLeftCorner The top-left corner of the block in world coordinates
     * @param renderable The visual representation of the block.
     */
    public Block(Vector2 topLeftCorner, Renderable renderable) {
        super(topLeftCorner, Vector2.ONES.mult(SIZE), renderable);
        setTag("block");
        physics().preventIntersectionsFromDirection(Vector2.ZERO);
        physics().setMass(GameObjectPhysics.IMMOVABLE_MASS);
    }
}
