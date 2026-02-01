package pepse.world;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;

import java.awt.Color;

/**
 * A class for creating the sky background of the game world
 * the sky is a single rectangle that covers the entire window and drawn in camera coordinates.
 *
 */
public class Sky {

    /**
     * The base color of the sky
     */
    private static final Color BASIC_SKY_COLOR = Color.decode("#80C6E5");

    /**
     * Creates the sky background object
     *
     * @param windowDimensions The window dimensions in pixels.
     * @return A {@link GameObject} representing the sky
     */
    public static GameObject create(Vector2 windowDimensions) {
        GameObject sky = new GameObject(
                Vector2.ZERO,
                windowDimensions,
                new RectangleRenderable(BASIC_SKY_COLOR));
        sky.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        sky.setTag("sky");
        return sky;
    }
}
