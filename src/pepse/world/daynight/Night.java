package pepse.world.daynight;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;

import java.awt.Color;

/**
 * Creates the night overlay effect
 * the night is implemented as a black full-screen rectangle whose opacity changes over time
 * creating a day-night cycle
 */
public class Night {

    /**
     * The maximum opacity reached at midnight
     */
    private static final Float MIDNIGHT_OPACITY = 0.5f;

    /**
     * creates a night overlay and animates its opacity
     *
     * @param windowDimensions the size of the window in pixels
     * @param cycleLength the length in seconds of a day-night cycle
     * @return A {@link GameObject} that represents the night overlay.
     */
    public static GameObject create(Vector2 windowDimensions, float cycleLength) {
        RectangleRenderable renderable = new RectangleRenderable(Color.BLACK);
        GameObject night = new GameObject(Vector2.ZERO, windowDimensions, renderable);
        night.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        night.setTag("night");

        new Transition<Float>(
                night,
                night.renderer()::setOpaqueness,
                0f,
                MIDNIGHT_OPACITY,
                Transition.CUBIC_INTERPOLATOR_FLOAT,
                cycleLength/2,
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                null);

        return night;
    }
}
