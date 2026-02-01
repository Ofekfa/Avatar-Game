package pepse.world.trees;

import danogl.GameObject;
import danogl.components.GameObjectPhysics;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;

import java.awt.*;

/**
 * A vertical immovable trunk GameObject representing the tree trunk
 *
 * a Trunk is implemented as a rectangle, width equals {@code Block.SIZE} with
 * immovable mass so other objects collide with it.
 */
public class Trunk extends GameObject {
    private static final Color TRUNK_COLOR = new Color(100, 50, 20);

    /**
     * Creates a new trunk instance
     *
     * The trunk's top-left corner is placed at {@code topLeftCorner} and its height is set to
     * {@code height}. The trunk width is fixed to {@link pepse.world.Block#SIZE}
     *
     * @param topLeftCorner the top-left corner of the trunk in world coordinates.
     * @param height the height (in pixels) of the trunk.
     */
    public Trunk(Vector2 topLeftCorner, float height) {
        super(topLeftCorner,
                new Vector2(pepse.world.Block.SIZE, height),
                new RectangleRenderable(TRUNK_COLOR));

        physics().preventIntersectionsFromDirection(Vector2.ZERO);
        physics().setMass(GameObjectPhysics.IMMOVABLE_MASS);
        setTag("trunk");
    }
}
